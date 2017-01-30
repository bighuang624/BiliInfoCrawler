package homework_5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Test {

	static String jsonURL1 = "http://api.bilibili.com/archive_rank/getarchiverankbypartion?callback=?&type=jsonp&tid=";
	static String jsonURL2 = "&pn=";
	static Connection conn = null;
	static int[] tids = { 28, 30, 31, 54, 59, 29, 130 };
	static String[] tnames = { "原创音乐", "VOCALOID·UTAU", "翻唱", "OP/ED/OST", "演奏", "三次元音乐", "音乐选集" };

	// 发送请求得到json文件
	public String getJson(int w, int pagenum) throws IOException {
		HttpClient httpClient = HttpClients.createDefault();
		HttpGet req = new HttpGet(jsonURL1 + tids[w] + jsonURL2 + pagenum);
		req.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
		req.addHeader("Accept-Encoding", "gzip,deflate");
		req.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
		req.addHeader("Content-Type", "text/html; charset=UTF-8");
		req.addHeader("User-Agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:50.0) Gecko/20100101 Firefox/50.0");
		CloseableHttpResponse resp = (CloseableHttpResponse) httpClient.execute(req);
		HttpEntity repEntity = resp.getEntity();
		String content = "[" + EntityUtils.toString(repEntity) + "]";
		return content;
	}

	// 从json文件中得到该类别视频总数
	public int getVideoNumber(String jsonContent) throws Exception {
		JSONArray array = JSONArray.fromObject(jsonContent);
		JSONObject object = null;
		int l = array.size();
		int videoNumber = 0;
		for (int i = 0; i < l; i++) {
			object = array.getJSONObject(i);
			JSONObject obj1 = (JSONObject) object.get("data");
			JSONObject obj2 = (JSONObject) obj1.get("page");
			videoNumber = Integer.parseInt(obj2.get("count").toString());
		}
		return videoNumber;
	}

	// 从json文件中获得视频相关数据
	public void handleJson(String jsonContent) throws Exception {
		JSONArray array = JSONArray.fromObject(jsonContent);
		JSONObject object = null;
		int l = array.size();
		for (int i = 0; i < l; i++) {
			object = array.getJSONObject(i);
			JSONObject obj1 = (JSONObject) object.get("data");
			JSONObject obj2 = (JSONObject) obj1.get("archives");
			for (int j = 0; j < 20; j++) {
				JSONObject obj3 = (JSONObject) obj2.get(String.valueOf(j));
				Bilibili video = new Bilibili();
				video.setAid(Integer.parseInt(obj3.get("aid").toString()));
				video.setTid(Integer.parseInt(obj3.get("tid").toString()));
				video.setTname((obj3.get("tname")).toString());
				video.setTitle((obj3.get("title")).toString());
				video.setAuthor((obj3.get("author")).toString());
				JSONObject obj4 = (JSONObject) obj3.get("stat");
				video.setCoin(Integer.parseInt(obj4.get("coin").toString()));
				video.setFavorite(Integer.parseInt(obj4.get("favorite").toString()));
				JDBC.insertBilibiliData(conn, video);
			}
		}
	}

	// 得到视频下载地址
	public String getDownloadPath(String urlPath) {
		try {
			HttpClient downHttpClient = HttpClients.createDefault();
			HttpGet downReq = new HttpGet(urlPath);
			downReq.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
			downReq.addHeader("Accept-Encoding", "gzip,deflate");
			downReq.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			downReq.addHeader("Content-Type", "text/html; charset=UTF-8");
			downReq.addHeader("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:50.0) Gecko/20100101 Firefox/50.0");
			HttpResponse downResp = downHttpClient.execute(downReq);
			HttpEntity downRepEntity = downResp.getEntity();
			String downContent = EntityUtils.toString(downRepEntity);
			Document doc = Jsoup.parse(downContent);
			Element a = doc.select("a[href*=http://www.bilibilijj.com/Files/DownLoad/]").first();
			return a.attr("href");
		} catch (NullPointerException e) {} 
		catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	// 下载视频
	public void getVideo(String downloadPath, String videoName) throws Exception {
		if (downloadPath != "") {
			URL url = new URL(downloadPath);
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setRequestMethod("GET");
			urlConn.setConnectTimeout(6000);
			if (urlConn.getResponseCode() == 200) {
				InputStream inputStream = urlConn.getInputStream();
				byte[] data = readInputStream(inputStream);
				if (videoName.contains("/")) {
					videoName = videoName.replaceAll("/", "");
				}
				File file = new File("doc/" + videoName + ".mp4");
				FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(data);
				outputStream.close();
			} else {
				System.out.println("---" + videoName + "下载失败");
			}
			System.out.println("---" + videoName + "下载完成");
		}
		else{
			System.out.println(videoName + "的下载链接用javascript处理，暂时无法获取");
		}
	}

	private byte[] readInputStream(InputStream inputStream) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = -1;
		while ((length = inputStream.read(buffer)) != -1) {
			byteArrayOutputStream.write(buffer, 0, length);
		}
		byteArrayOutputStream.close();
		inputStream.close();

		return byteArrayOutputStream.toByteArray();
	}

	public void programStart() throws Exception {
		for (int w = 0; w < tids.length; w++) {
			Thread biliThread = new BiliThread(w);
			biliThread.start();
		}
	}

	class BiliThread extends Thread {
		int w;

		BiliThread(int w) {
			this.w = w;
		}

		@Override
		public void run() {

			try {
				conn = JDBC.getConnection();
				int videoNum;
				int pageNum;

				synchronized (this) {
					String content1 = getJson(w, 1);
					handleJson(content1);
					videoNum = getVideoNumber(content1);
					pageNum = videoNum / 20;
				}
				System.out.println("bilibili共有" + videoNum + "个" + tnames[w] + "视频");
				System.out.println(tnames[w] + "第1页的视频信息已被爬取");
				try {
					Thread.currentThread().sleep(600); // 暂停0.6秒后程序继续执行
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for (int i = 2; i < pageNum; i++) {
					synchronized (this) {
						String content = getJson(w, i);
						handleJson(content);
					}
					System.out.println(tnames[w] + "第" + i + "页的视频信息已被爬取");
					try {
						Thread.currentThread().sleep(900); // 暂停0.9秒后程序继续执行
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("---" + tnames[w] + "所有视频数据抓取完毕");

				synchronized (this) {
					String[] info = JDBC.searchTop3(conn, tids[w]);
					for (int k = 0; k < 3; k++) {
						String downloadPath = getDownloadPath("http://www.ibilibili.com/video/av" + info[k] + "/");
						System.out.println("---开始下载" + info[k + 3] + "，该视频被收藏数：" + info[k + 6]);
						getVideo(downloadPath, info[k + 3]);
					}
				}
				System.out.println("---" + tnames[w] + "收藏数前三的视频下载完毕\n");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
