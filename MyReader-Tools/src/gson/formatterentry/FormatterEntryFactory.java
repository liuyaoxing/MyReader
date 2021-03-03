//package gson.formatterentry;
//
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//import com.google.gson.Gson;
//
//public class FormatterEntryFactory {
//
//	public static void main(String[] args) {
////		{
////			FormatterEntry entry = new FormatterEntry();
////			entry.setRemove_tags(new String[] {});
////			entry.setAuthor_select(new String[] {});
////			entry.setDate_select(new String[] {});
////			entry.setContent_select(new String[] {});
////			entry.setComments_select(new String[] {});
////			entryMap.put("jianshu.com", entry);
////		}
//
//		Map<String, FormatterEntry> entryMap = new LinkedHashMap<String, FormatterEntry>();
//		{// cnbeta.com
//			FormatterEntry cnBeta = new FormatterEntry();
//			cnBeta.setDate_select(new String[] { "div.meta > span", "span.pt_info" });
//			cnBeta.setAuthor_select(new String[] { "span.source" });
//			cnBeta.setContent_select(new String[] { "div.post_content", "div.article-content" });
//			entryMap.put("cnbeta.com", cnBeta);
//		}
//
//		{ // ithome.com
//			FormatterEntry ithome = new FormatterEntry();
//			ithome.setRemove_tags(new String[] { "div.user-hd" });
//			ithome.setAuthor_select(new String[] { "span.news-author", "span.source_baidu", "span#source_baidu" });
//			ithome.setDate_select(new String[] { "span.news-time", "span.pubtime_baidu", "span#pubtime_baidu" });
//			ithome.setContent_select(new String[] { "div.news-content", "div.post_content", "div#paragraph" });
//			ithome.setComments_select(new String[] { "div.comment" });
//			entryMap.put("ithome.com", ithome);
//		}
//
//		{// jianshu.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "div.image-container-fill" });
//			entry.setAuthor_select(new String[] { "div.article-info div.name", "div.article-info span.name" });
//			entry.setDate_select(new String[] { "div.article-info div.meta", "div.article-info span.publish-time" });
//			entry.setContent_select(new String[] { "div.content", "div.show-content", "div.show-content-free" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("jianshu.com", entry);
//		}
//
//		{// zhangzisi.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] {});
//			entry.setAuthor_select(new String[] { "¶¡¶¡" });
//			entry.setDate_select(new String[] { "time.new" });
//			entry.setContent_select(new String[] { "article.article-content" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("zhangzisi.com", entry);
//			entryMap.put("zhangzisi.cc", entry);
//		}
//
//		{// wallstreetcn.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] {});
//			entry.setAuthor_select(new String[] { "span.author-name", "span.line-clamp" });
//			entry.setDate_select(new String[] { "" });
//			entry.setContent_select(new String[] { "div.article_content", "div.articleDetail" });
//			entry.setComments_select(new String[] { "div.article_comments", "div.comments-item" });
//			entryMap.put("wallstreetcn.com", entry);
//		}
//
//		{// awtmt.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] {});
//			entry.setAuthor_select(new String[] { "a.article-author-name" });
//			entry.setDate_select(new String[] { "span.article-create-time" });
//			entry.setContent_select(new String[] { "div.-article-content", "div.article-content" });
//			entry.setComments_select(new String[] { "div.comments-content" });
//			entryMap.put("awtmt.com", entry);
//		}
//
//		{// bh.sb bohaishibei.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] {});
//			entry.setAuthor_select(new String[] { "ul.article-meta > li" });
//			entry.setDate_select(new String[] {});
//			entry.setContent_select(new String[] { "article.article-content" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("bh.sb", entry);
//			entryMap.put("bohaishibei.com", entry);
//		}
//
//		{// thepaper.cn
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "div[style=\"display: block\"]" });
//			entry.setAuthor_select(new String[] { "ul.article-meta > li" });
//			entry.setDate_select(new String[] {});
//			entry.setContent_select(new String[] { "div.news_content", "body" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("thepaper.cn", entry);
//		}
//
//		{// 36kr.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "div[style=\"display: block\"]" });
//			entry.setAuthor_select(new String[] { "ul.article-meta > li" });
//			entry.setDate_select(new String[] { "span.author-update-time", "abbr.time", "span.time" });
//			entry.setContent_select(new String[] { "div.news_content", "body" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("36kr.com", entry);
//		}
//
//		{// infoq.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] {});
//			entry.setAuthor_select(new String[] { "span.editorlink" });
//			entry.setDate_select(new String[] { "span.heading_author" });
//			entry.setContent_select(new String[] { "div.article_content" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("infoq.com", entry);
//		}
//
//		{// dapenti.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "ins" });
//			entry.setAuthor_select(new String[] { "span.editorlink" });
//			entry.setDate_select(new String[] { "span.heading_author" });
//			entry.setContent_select(new String[] {"div.oblog_text", "table p", "body > p" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("dapenti.com", entry);
//		}
//
//		{// jandan.net
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] {});
//			entry.setTitle_select(new String[] { "h1.thetitle" });
//			entry.setAuthor_select(new String[] { "span.editorlink" });
//			entry.setDate_select(new String[] { "span.heading_author" });
//			entry.setContent_select(new String[] { "div.entry", "div.post" });
//			entry.setComments_select(new String[] { "div.comments", "div#comments", "ol.commentlist" });
//			entryMap.put("jandan.net", entry);
//		}
//
//		{// fulibus.net
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "div#focusslide", "blockquote" });
//			entry.setAuthor_select(new String[] { "div.article-meta" });
//			entry.setDate_select(new String[0]);
//			entry.setContent_select(new String[] { "article.article-content", "div.content", "div.content-wrap" });
//			entry.setComments_select(new String[] { "div#comments" });
//			entryMap.put("fulibus.net", entry);
//		}
//
//		{// douban.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "div#focusslide", "blockquote" });
//			entry.setAuthor_select(new String[] { "a.note-author", "span.info" });
//			entry.setDate_select(new String[] { "span.pub-date", "span.timestamp" });
//			entry.setContent_select(new String[] { "div.note", "section.note-content" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("douban.com", entry);
//		}
//
//		{// coyee.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[0]);
//			entry.setAuthor_select(new String[0]);
//			entry.setDate_select(new String[] { "div.time_s" });
//			entry.setContent_select(new String[] { "div.post > p" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("coyee.com", entry);
//		}
//
//		{// xw.qq.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "div.sizer" });
//			entry.setAuthor_select(new String[] { "a.author" });
//			entry.setDate_select(new String[] { "div.subtime" });
//			entry.setContent_select(new String[] { "div#article_body" });
//			entry.setComments_select(new String[] { "div.comments" });
//			entryMap.put("xw.qq.com", entry);
//		}
//
//		{// news.qq.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "div.sizer" });
//			entry.setAuthor_select(new String[] { "span.a_source" });
//			entry.setDate_select(new String[] { "span.a_time" });
//			entry.setContent_select(new String[] { "div#Cnt-Main-Article-QQ" });
//			entry.setComments_select(new String[] { "div.comment-short" });
//			entryMap.put("news.qq.com", entry);
//		}
//
//		{// new.qq.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "div.sizer" });
//			entry.setAuthor_select(new String[] { "a.author" });
//			entry.setDate_select(new String[] { "div.year" });
//			entry.setContent_select(new String[] { "div.content-article" });
//			entry.setComments_select(new String[] { "div.comments" });
//			entryMap.put("new.qq.com", entry);
//		}
//
//		{// health.sina.com.cn
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "div.sizer" });
//			entry.setAuthor_select(new String[] { "span#media_name" });
//			entry.setDate_select(new String[] { "span.pub_date" });
//			entry.setContent_select(new String[] { "div#artibody", "div.blkContainer" });
//			entry.setComments_select(new String[] { "div.comment_item" });
//			entryMap.put("health.sina.com.cn", entry);
//		}
//
//		{// blog.sina.com.cn
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] {});
//			entry.setAuthor_select(new String[] { "span.a_source" });
//			entry.setDate_select(new String[] { "span.time" });
//			entry.setContent_select(new String[] { "div#sina_keyword_ad_area2", "div.articalContent" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("blog.sina.com.cn", entry);
//		}
//
//		{// tech.sina.com.cn
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] {});
//			entry.setAuthor_select(new String[] { "a.date-source" });
//			entry.setDate_select(new String[] { "div.date" });
//			entry.setContent_select(new String[] { "div#artibody", "div.article" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("tech.sina.com.cn", entry);
//		}
//
//		{// hexun.com
//			FormatterEntry entry = new FormatterEntry();
//			entry.setRemove_tags(new String[] { "a.textbtngotoapp" });
//			entry.setAuthor_select(new String[] { "a.date-source" });
//			entry.setDate_select(new String[] { "div.date" });
//			entry.setContent_select(new String[] { "div#artibody", "div.article" });
//			entry.setComments_select(new String[] {});
//			entryMap.put("tech.sina.com.cn", entry);
//		}
//		
//		
//		System.out.println(new Gson().toJson(entryMap));
//	}
//}
