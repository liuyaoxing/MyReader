package gson.preferencehost;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import gson.formatterentry.FormatterEntry;

public class PreferenceHostFactory {

	public static void main(String[] args) {
		List<PreferenceHost> hostList = new ArrayList<PreferenceHost>();
//		{
//			PreferenceHost host1 = new PreferenceHost();
//			host1.setUrl("fuliba.net");
//			host1.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
//					"data-canonical-src", "data-link" });
//			FormatterEntry toutiao = new FormatterEntry();
//			toutiao.setDate_select(new String[] { "div.article-sub"});
//			toutiao.setAuthor_select(new String[] {});
//			toutiao.setContent_select(new String[] { "article", "article p" });
//			toutiao.setRemove_tags(new String[] {"img.ls_lazyimg"});
//			host1.setFormatterEntry(toutiao);
//			hostList.add(host1);
//		}
		{
			PreferenceHost host1 = new PreferenceHost();
			host1.setUrl("toutiao.com");
			host1.setSaveImages(new String[] { "src", "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry toutiao = new FormatterEntry();
			toutiao.setDate_select(new String[] { "div.article-sub" });
			toutiao.setAuthor_select(new String[] {});
			toutiao.setContent_select(new String[] { "article", "article p" });
			host1.setFormatterEntry(toutiao);
			hostList.add(host1);
		}
		{
			PreferenceHost host1 = new PreferenceHost();
			host1.setUrl("toutiaocdn.com");
			host1.setSaveImages(new String[] { "src", "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry toutiao = new FormatterEntry();
			toutiao.setTitle_select(new String[] { "h1.article__title" });
			toutiao.setDate_select(new String[] { "div.author-info" });
			toutiao.setAuthor_select(new String[] {});
			toutiao.setRemove_tags(new String[] { "img.onerror" });
			toutiao.setContent_select(new String[] { "article", "article p" });
			host1.setFormatterEntry(toutiao);
			hostList.add(host1);
		}
		{
			PreferenceHost host1 = new PreferenceHost();
			host1.setUrl("cnbeta.com");
			host1.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry cnBeta = new FormatterEntry();
			cnBeta.setDate_select(new String[] { "div.meta > span", "span.pt_info" });
			cnBeta.setAuthor_select(new String[] { "span.source" });
			cnBeta.setContent_select(new String[] { "div.post_content", "div.article-content" });
			host1.setFormatterEntry(cnBeta);
			hostList.add(host1);
		}
		{
			PreferenceHost host2 = new PreferenceHost();
			host2.setUrl("ithome.com");
			host2.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry ithome = new FormatterEntry();
			ithome.setRemove_tags(new String[] { "div.user-hd" });
			ithome.setAuthor_select(new String[] { "span.news-author", "span.source_baidu", "span#source_baidu" });
			ithome.setDate_select(new String[] { "span.news-time", "span.pubtime_baidu", "span#pubtime_baidu" });
			ithome.setContent_select(new String[] { "div.news-content", "div.post_content", "div#paragraph" });
			ithome.setComments_select(new String[] { "div.comment" });
			host2.setFormatterEntry(ithome);
			hostList.add(host2);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("jianshu.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "div.image-container-fill" });
			entry.setAuthor_select(new String[] { "div.article-info div.name", "div.article-info span.name" });
			entry.setDate_select(new String[] { "div.article-info div.meta", "div.article-info span.publish-time" });
			entry.setContent_select(new String[] { "div.content", "div.show-content", "div.show-content-free" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("zhangzisi.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] {});
			entry.setAuthor_select(new String[] { "����" });
			entry.setDate_select(new String[] { "time.new" });
			entry.setContent_select(new String[] { "article.article-content" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("zhangzs.com");
			host.setSaveImages(new String[] { "data-original" });
			host.setDeleteRelates(new String[] { "div.entry-copyright", "div.entry-footer" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "[name=MobileOptimized]" });
			entry.setAuthor_select(new String[] { "����" });
			entry.setDate_select(new String[] { "div.entry-info span" });
			entry.setContent_select(new String[] { "div.entry-content" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("wallstreetcn.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] {});
			entry.setAuthor_select(new String[] { "span.author-name", "span.line-clamp" });
			entry.setDate_select(new String[] { "" });
			entry.setContent_select(new String[] { "div.article_content", "div.articleDetail" });
			entry.setComments_select(new String[] { "div.article_comments", "div.comments-item" });
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("awtmt.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] {});
			entry.setAuthor_select(new String[] { "a.article-author-name" });
			entry.setDate_select(new String[] { "span.article-create-time" });
			entry.setContent_select(new String[] { "div.-article-content", "div.article-content" });
			entry.setComments_select(new String[] { "div.comments-content" });
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("bh.sb");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] {});
			entry.setAuthor_select(new String[] { "ul.article-meta > li" });
			entry.setDate_select(new String[] {});
			entry.setContent_select(new String[] { "article.article-content" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("thepaper.cn");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "div[style=\"display: block\"]" });
			entry.setAuthor_select(new String[] { "ul.article-meta > li" });
			entry.setDate_select(new String[] { "span.author-update-time", "abbr.time", "span.time" });
			entry.setContent_select(new String[] { "div.news_content", "body" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("36kr.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "div[style=\"display: block\"]" });
			entry.setAuthor_select(new String[] { "ul.article-meta > li" });
			entry.setDate_select(new String[] { "span.author-update-time", "abbr.time", "span.time" });
			entry.setContent_select(new String[] { "div.news_content", "body" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("infoq.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] {});
			entry.setAuthor_select(new String[] { "span.editorlink" });
			entry.setDate_select(new String[] { "span.heading_author" });
			entry.setContent_select(new String[] { "div.article_content" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("dapenti.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "ins" });
			entry.setAuthor_select(new String[] { "span.editorlink" });
			entry.setDate_select(new String[] { "span.heading_author" });
			entry.setContent_select(new String[] { "div.oblog_text", "table p", "body > p" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("jandan.net");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] {});
			entry.setTitle_select(new String[] { "h1.thetitle" });
			entry.setAuthor_select(new String[] { "span.editorlink" });
			entry.setDate_select(new String[] { "span.heading_author" });
			entry.setContent_select(new String[] { "div.entry", "div.post" });
			entry.setComments_select(new String[] { "div.comments", "div#comments", "ol.commentlist" });
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("fulibus.net");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "div#focusslide", "blockquote" });
			entry.setAuthor_select(new String[] { "div.article-meta" });
			entry.setDate_select(new String[0]);
			entry.setContent_select(new String[] { "article.article-content", "div.content", "div.content-wrap" });
			entry.setComments_select(new String[] { "div#comments" });
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("douban.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "div#focusslide", "blockquote" });
			entry.setAuthor_select(new String[] { "a.note-author", "span.info" });
			entry.setDate_select(new String[] { "span.pub-date", "span.timestamp" });
			entry.setContent_select(new String[] { "div.note", "section.note-content" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("coyee.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[0]);
			entry.setAuthor_select(new String[0]);
			entry.setDate_select(new String[] { "div.time_s" });
			entry.setContent_select(new String[] { "div.post > p" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("xw.qq.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "div.sizer" });
			entry.setAuthor_select(new String[] { "a.author" });
			entry.setDate_select(new String[] { "div.subtime" });
			entry.setContent_select(new String[] { "div#article_body" });
			entry.setComments_select(new String[] { "div.comments" });
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("news.qq.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "div.sizer" });
			entry.setAuthor_select(new String[] { "span.a_source" });
			entry.setDate_select(new String[] { "span.a_time" });
			entry.setContent_select(new String[] { "div#Cnt-Main-Article-QQ" });
			entry.setComments_select(new String[] { "div.comment-short" });
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("new.qq.com");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "div.sizer" });
			entry.setAuthor_select(new String[] { "a.author" });
			entry.setDate_select(new String[] { "div.year" });
			entry.setContent_select(new String[] { "div.content-article" });
			entry.setComments_select(new String[] { "div.comments" });
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("health.sina.com.cn");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "div.sizer" });
			entry.setAuthor_select(new String[] { "span#media_name" });
			entry.setDate_select(new String[] { "span.pub_date" });
			entry.setContent_select(new String[] { "div#artibody", "div.blkContainer" });
			entry.setComments_select(new String[] { "div.comment_item" });
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("blog.sina.com.cn");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] {});
			entry.setAuthor_select(new String[] { "span.a_source" });
			entry.setDate_select(new String[] { "span.time" });
			entry.setContent_select(new String[] { "div#sina_keyword_ad_area2", "div.articalContent" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("tech.sina.com.cn");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] {});
			entry.setAuthor_select(new String[] { "a.date-source" });
			entry.setDate_select(new String[] { "div.date" });
			entry.setContent_select(new String[] { "div#artibody", "div.article" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}
		{
			PreferenceHost host = new PreferenceHost();
			host.setUrl("tech.sina.com.cn");
			host.setSaveImages(new String[] { "ess-data", "original", "data-canonical-src", "data-src",
					"data-canonical-src", "data-link" });
			FormatterEntry entry = new FormatterEntry();
			entry.setRemove_tags(new String[] { "a.textbtngotoapp" });
			entry.setAuthor_select(new String[] { "a.date-source" });
			entry.setDate_select(new String[] { "div.date" });
			entry.setContent_select(new String[] { "div#artibody", "div.article" });
			entry.setComments_select(new String[] {});
			host.setFormatterEntry(entry);
			hostList.add(host);
		}

		System.out.println(new Gson().toJson(hostList.toArray(new PreferenceHost[0])));

		String jsonStr = "[{\"url\":\"cnbeta.com\",\"formatterEntry\":{\"date_select\":[\"div.meta \\u003e span\",\"span.pt_info\"],\"author_select\":[\"span.source\"],\"content_select\":[\"div.post_content\",\"div.article-content\"],\"script_remove\":true},\"saveImages\":[\"ess-data\"],\"useDownloadManager\":false},{\"url\":\"ithome.com\",\"formatterEntry\":{\"date_select\":[\"span.news-time\",\"span.pubtime_baidu\",\"span#pubtime_baidu\"],\"author_select\":[\"span.news-author\",\"span.source_baidu\",\"span#source_baidu\"],\"content_select\":[\"div.news-content\",\"div.post_content\",\"div#paragraph\"],\"comments_select\":[\"div.comment\"],\"remove_tags\":[\"div.user-hd\"],\"script_remove\":true},\"saveImages\":[\"ess-data\"],\"useDownloadManager\":false}]\r\n";
		PreferenceHost[] hosts = new Gson().fromJson(jsonStr, PreferenceHost[].class);
		System.out.println(hosts);
	}
}
