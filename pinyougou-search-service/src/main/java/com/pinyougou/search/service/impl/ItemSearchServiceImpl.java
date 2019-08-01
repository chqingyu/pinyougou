package com.pinyougou.search.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService{

	@Autowired
	private SolrTemplate solrTemplate;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public Map search(Map searchMap) {
		
		Map map = new ConcurrentHashMap<>();
		
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		/*Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query , TbItem.class);
		
		map.put("rows", page.getContent());
		*/
		
		
		// 查询高亮结果列表
		map.putAll(searchList(searchMap));
		
		// 分组查询,商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		
		// 查询品牌和规格列表,如果用户选了商品分类,就显示用户选择的商品分类下的规格和品牌,如果用户没有选商品分类,则按查询出来的第一个商品分类显示品牌和规格
		String category = (String) searchMap.get("category");
		if(!category.equals("")){
			map.putAll(searchBrandAndSpecList(category));
		}else{
			// 如果用户没选,需要先判断该关键字下的商品分类的集合是否大于0
			if(categoryList.size() > 0){
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		
		return map;
		
	}
	
	private Map searchList(Map searchMap){
		
		Map map = new ConcurrentHashMap<>();
		
HighlightQuery query = new SimpleHighlightQuery();
		
		// 怎样去设置高亮
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title"); // 高亮域
		highlightOptions.setSimplePrefix("<em style='color:red'>"); // 前缀
		highlightOptions.setSimplePostfix("</em>");
		query.setHighlightOptions(highlightOptions);
		
		// 关键字查询(搜索框)
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		// 如果用户选择了商品分类的过滤
		if(!"".equals(searchMap.get("category"))){
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria );
			query.addFilterQuery(filterQuery );
		}
		
		// 如果用户选择了品牌的过滤
		if(!"".equals(searchMap.get("brand"))){
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria );
			query.addFilterQuery(filterQuery );
		}
		
		// 如果用户选择了规格
		if(searchMap.get("spec")!=null){
			Map<String,String> specMap = (Map) searchMap.get("spec");
			for(String key : specMap.keySet()){
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria );
				query.addFilterQuery(filterQuery );
			}
		}
		
		if(!"".equals(searchMap.get("price"))){
			String[] price = ((String) searchMap.get("price")).split("-");
			// 如果最低价格不等于0
			if(!price[0].equals("0")){
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria );
				query.addFilterQuery(filterQuery);
			}
			
			
			// 如果最高价格是* 那么价格的查询范围是>price[0]
			// 最高价格不等于*
			if(!price[1].equals("*")){
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").lessThan(price[1]);
				filterQuery.addCriteria(filterCriteria );
				query.addFilterQuery(filterQuery);
			}
		}
		
		
		// 当前页码
		Integer pageNo = (Integer) searchMap.get("pageNo");
		if(pageNo==null){
			pageNo=1;
		}
		// 当前页大小
		Integer pageSize = (Integer) searchMap.get("pageSize");
		if(pageSize==null){
			pageSize = 20;
		}
		
		query.setOffset((pageNo-1)*pageSize); // 开始索引
		query.setRows(pageSize);
		
		// 按所选字段排序
		String sortValue = (String) searchMap.get("sort");  // 升序ASC,降序DESC
		String sortField = (String) searchMap.get("sortField");
		if(sortValue!=null && !sortValue.equals("")){
			if(sortValue.equals("ASC")){
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")){
				Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
				query.addSort(sort);
			}
		}
		
		
		// 获取高亮结果集
		// 高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		
		// 高亮入口对象的集合(每条记录的高亮入口集合)
		List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
		for (HighlightEntry<TbItem> entry : highlighted) {
			
			// 高亮集合(高亮域的个数)
			List<Highlight> highlightList = entry.getHighlights();
			
			/*for (Highlight h : highlights) {
				List<String> snipplets = h.getSnipplets(); // 每个域有可能存储多值(因为可能有其他域的结果)
				System.out.println(snipplets);
			}*/
			
			if(highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0){
				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}
			
		}
		map.put("rows", page.getContent());
		map.put("totalPage", page.getTotalPages()); // 总页数
		map.put("total", page.getTotalElements()); // 总记录数
		return map;
	}
	
	
	private List searchCategoryList(Map searchMap){
		
		List<String> list = new CopyOnWriteArrayList<>();
		Query query = new SimpleQuery("*:*");
		
		// 根据关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		// 设置分组选项(可以加多列)
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category"); // group by
		query.setGroupOptions(groupOptions);
		
		// 获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		// 获取分组结果对象
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		// 获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : entryList) {
			entry.getGroupValue();
			list.add(entry.getGroupValue()); // 将分组的结果添加到返回值中
		}
		
		return list;
	}

	/**
	 * 根据商品分类名称查询品牌和规格列表
	 * @param category
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		
		Map map = new ConcurrentHashMap<>();
		// 根据商品分类名称的到模版id
		Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if(templateId != null){
			// 根据模版id获取品牌列表
			List brandList =(List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);
			
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);
			
		}
		
		return map;
		
	}

	@Override
	public void importList(List list) {
		
		if(list!=null && list.size()!=0){
			solrTemplate.saveBeans(list);
			solrTemplate.commit();
		}
		
	}

	@Override
	public void deleteByGoodsIds(List goodsIds) {
		
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria );
		solrTemplate.delete(query );
		solrTemplate.commit();
	}
}
