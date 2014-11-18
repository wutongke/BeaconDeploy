package sortListView;

import java.io.Serializable;
 

public class SortModel implements Serializable{  
  
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;   //显示的数据  
    private String sortLetters;  //显示数据拼音的首字母  
    private String extraInfo; //附带的额外信息
      
    public String getExtraInfo() {
		return extraInfo;
	}
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	public String getName() {
        return name;  
    }  
    public void setName(String name) {  
        this.name = name;  
    }  
    public String getSortLetters() {  
        return sortLetters;  
    }  
    public void setSortLetters(String sortLetters) {  
        this.sortLetters = sortLetters;  
    }  
    
    
}  