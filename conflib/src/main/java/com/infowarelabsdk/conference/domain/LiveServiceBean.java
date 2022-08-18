package com.infowarelabsdk.conference.domain;

import com.infowarelabsdk.conference.transfer.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * 聊天服务信息
 *
 * @author Fred
 *
 */
public class LiveServiceBean {

//	"confId": 100000000034128,
//			"subject": "关会测试的会议",
//			"ip": "172.16.6.38",
//			"liveChatUrl": "ws://172.16.6.38",
//			"hls": "http://192.168.2.123:9037/live/hongshanshu100000000034128.m3u8?token=0b52b703d36583621d7e334546d7f31a6dd03f70b8a6ea2d926f4754615a4fc0dbd43f857f0d86833557ecaca5ca0371c504fd48d1a875a801e287d620015f4b1e476f11220ae89f39a2e9941155dcc6c08b0775108345f8beac72042f11ee5d7cc6377b0044fadca9906449cd1b7db6295cadb6a5c40d11bd0183091ed178cb510c5ba82906bf8c1b550ef623411a74",
//			"token": "130d28dfd21c3efd1d8408a151da4d7e",//appSceret
//			"liveChatContextPath": "/live-chat",
//			"success": true,
//			"conferenceStatus": "START",
//			"visitUid": "2ac109a832764bbe83d9a0b5fa467766",//uid
//			"nickType": 0,
//			"appId": "b4b1586f7872477291500b4be1e4394e",
//			"posterIsShow": false,
//			"nickname": null,
//			"poster": "",
//			"timestamp": "1655892381"


	private String confId;

	private String subject;

	private String ip;

	private String liveChatUrl = "";

	private String hls = "";

	private String token = "";

	private String liveChatContextPath = "";

	private boolean success = true;

	private String conferenceStatus = "";

	private String visitUid = "";

	private int nickType = 0;

	private String appId = "";

	private boolean posterIsShow = false;

	private String nickname;

	private String poster;

	private String timestamp;

	public boolean initFromJson(JSONObject json) {
		if(json==null)return false;

		confId = json.optString("confId");
		subject = json.optString("subject");
		ip = json.optString("ip");
		liveChatUrl = json.optString("liveChatUrl");
		hls = json.optString("hls");
		token = json.optString("token");
		liveChatContextPath = json.optString("liveChatContextPath");
		success = json.optBoolean("success");
		conferenceStatus = json.optString("conferenceStatus");
		visitUid = json.optString("visitUid");
		nickType = json.optInt("nickType");
		appId = json.optString("appId");
		posterIsShow = json.optBoolean("posterIsShow");
		nickname = json.optString("nickname");
		poster = json.optString("poster");
		timestamp = json.optString("timestamp");

		return true;
	}

	public String getConfId() {
		return confId;
	}

	public void setConfId(String confId) {
		this.confId = confId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLiveChatUrl() {
		return liveChatUrl;
	}

	public void setLiveChatUrl(String liveChatUrl) {
		this.liveChatUrl = liveChatUrl;
	}

	public String getHls() {
		return hls;
	}

	public void setHls(String hls) {
		this.hls = hls;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getLiveChatContextPath() {
		return liveChatContextPath;
	}

	public void setLiveChatContextPath(String liveChatContextPath) {
		this.liveChatContextPath = liveChatContextPath;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getConferenceStatus() {
		return conferenceStatus;
	}

	public void setConferenceStatus(String conferenceStatus) {
		this.conferenceStatus = conferenceStatus;
	}

	public String getVisitUid() {
		return visitUid;
	}

	public void setVisitUid(String visitUid) {
		this.visitUid = visitUid;
	}

	public int getNickType() {
		return nickType;
	}

	public void setNickType(int nickType) {
		this.nickType = nickType;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public boolean isPosterIsShow() {
		return posterIsShow;
	}

	public void setPosterIsShow(boolean posterIsShow) {
		this.posterIsShow = posterIsShow;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPoster() {
		return poster;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
