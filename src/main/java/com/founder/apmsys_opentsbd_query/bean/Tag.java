package com.founder.apmsys_opentsbd_query.bean;

import org.springframework.util.Assert;


public class Tag {

	public static final String CUSTOM_TAG_PREFIX = "#";
	public static final String SYS_HOST = "host";
	public static final String SYS_AGENT = "@agent";
	
	private String tagK;
	private String tagV;
	private Long   time;//创建时间
	
	public static Tag createHostTag(String hostName){
		Tag tag = new Tag();
		tag.setTagK("host");
		tag.setTagV(hostName);
		return tag;
	}
	
	public boolean isAgentTag(){
		return isAgentTag(tagK);
	}
	//这个接口无法开放
	private static boolean isAgentTag(String tagK){
		Assert.notNull(tagK,"cannot compare tagK ,tagK == null");
		if(SYS_AGENT.equals(tagK)){
			return true;
		}
		return false;
	}
	
	public boolean isHostTag(){
		return isHostTag(tagK);
	}
	public static boolean isHostTag(String tagK){
		Assert.notNull(tagK,"cannot compare tagK ,tagK == null");
		if(SYS_HOST.equals(tagK)){
			return true;
		}
		return false;
	}
	
	public boolean isCustomTag (){
		return isCustomTag(tagK);
	}
	public static boolean isCustomTag (String tagK){
		Assert.notNull(tagK, "无法判断一个为null的tagK是 CustomTag or sysTag");
		if(tagK.startsWith(CUSTOM_TAG_PREFIX)){
			return true;
		}
		return false;
	}
	
	public String[] getAgentTagKs(){
		Assert.isTrue(isAgentTag(), "不是一个标准的探针tag, tagK:"+tagK);
		return tagV.split(",");
	}
	
	@Override
	public String toString() {
		return "Tag [tagK=" + tagK + ", tagV=" + tagV + ", time=" + time + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tagK == null) ? 0 : tagK.hashCode());
		result = prime * result + ((tagV == null) ? 0 : tagV.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		if (tagK == null) {
			if (other.tagK != null)
				return false;
		} else if (!tagK.equals(other.tagK))
			return false;
		if (tagV == null) {
			if (other.tagV != null)
				return false;
		} else if (!tagV.equals(other.tagV)){
			return false;
		}
		return true;
	}
	public String getTagK() {
		return tagK;
	}
	public void setTagK(String tagK) {
		this.tagK = tagK;
	}
	public String getTagV() {
		return tagV;
	}
	public void setTagV(String tagV) {
		this.tagV = tagV;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
}
