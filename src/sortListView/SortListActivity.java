package sortListView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sortListView.SideBar.OnTouchingLetterChangedListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.scaletest.R;
  
public class SortListActivity extends Activity {  
    private ListView sortListView;  
    private SideBar sideBar;
    
    /**
     * 显示字母的TextView 
     */  
    private TextView dialog;  
    private SortAdapter adapter;  
   // private ClearEditText mClearEditText;  
    private  EditText mClearEditText;
      
    /** 
     * 汉字转换成拼音的类 
     */  
    private CharacterParser characterParser;  
    
    private List<SortModel> sourceDateList;  
      
    /** 
     * 根据拼音来排列ListView里面的数据类 
     */  
    private PinyinComparator pinyinComparator;  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.sort_list_layout);
        initViews();  
    }  
  
    @SuppressWarnings("unchecked")
	private void initViews() {  
        //实例化汉字转拼音类  
        characterParser = CharacterParser.getInstance();  
          
        pinyinComparator = new PinyinComparator();  
          
        sideBar = (SideBar) findViewById(R.id.sidebar);  
        dialog = (TextView) findViewById(R.id.dialog);  
        sideBar.setTextView(dialog);  
          
        //设置右侧触摸监听  
        sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
            @Override  
            public void onTouchingLetterChanged(String s) {  
                //该字母首次出现的位置  
                int position = adapter.getPositionForSection(s.charAt(0));  
                if(position != -1){  
                    sortListView.setSelection(position);//滚动到指定位置  
                }  
                  
            }  
        });  
          
        sortListView = (ListView) findViewById(R.id.lv);  
        sortListView.setOnItemClickListener(new OnItemClickListener() {  
            @Override  
            public void onItemClick(AdapterView<?> parent, View view,  
                    int position, long id) {  
                //这里要利用adapter.getItem(position)来获取当前position所对应的对象  
            	String itemName=((SortModel)adapter.getItem(position)).getName();
                String extraData=((SortModel)adapter.getItem(position)).getExtraInfo();
                
                Intent resultIntent = new Intent();
				resultIntent.putExtra("itemName", itemName);  
				resultIntent.putExtra("extraData",extraData);  
				setResult(RESULT_OK, resultIntent);  
				SortListActivity.this.finish();  
            }  
        });  
          
        
     
        sourceDateList = filledData();  
          
        // 根据a-z进行排序源数据  
        Collections.sort(sourceDateList, pinyinComparator);
        
        adapter = new SortAdapter(this, sourceDateList);  
        
        sortListView.setAdapter(adapter);  
          
        mClearEditText = (EditText) findViewById(R.id.filter_edit);  
          
        //根据输入框输入值的改变来过滤搜索  
        mClearEditText.addTextChangedListener(new TextWatcher() {  
            @Override  
            public void onTextChanged(CharSequence s, int start, int before, int count) {  
                //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表  
                filterData(s.toString());  
            }  
              
            @Override  
            public void beforeTextChanged(CharSequence s, int start, int count,  
                    int after) {  
            }  
              
            @Override  
            public void afterTextChanged(Editable s) {  
            }  
        });  
    }  
  
  
    /** 
     * 为ListView填充数据 
     * @param date 
     * @return 
     */  
    private List<SortModel> filledData(){  
    	List<SortModel> mSortList =  (List<SortModel>) getIntent().getSerializableExtra("dataList"); 
        for (SortModel sortModel : mSortList) {
        	//汉字转换成拼音  
            String pinyin = characterParser.getSelling(sortModel.getName());  
            String sortString = pinyin.substring(0, 1).toUpperCase();  
              
            // 正则表达式，判断首字母是否是英文字母  
            if(sortString.matches("[A-Z]")){  
                sortModel.setSortLetters(sortString.toUpperCase());  
            }else{  
                sortModel.setSortLetters("#");  
            }  
		}
        return mSortList;
    }  
      
    /** 
     * 根据输入框中的值来过滤数据并更新ListView 
     * @param filterStr 
     */  
    private void filterData(String filterStr) {  
        List<SortModel> filterDateList = new ArrayList<SortModel>();  
  
        if (TextUtils.isEmpty(filterStr)) {  
            filterDateList = sourceDateList;  
        } else {  
            filterDateList.clear();  
            for (SortModel sortModel : sourceDateList) {  
                String name = sortModel.getName();  
                if (name.toUpperCase().indexOf(  
                        filterStr.toString().toUpperCase()) != -1  
                        || characterParser.getSelling(name).toUpperCase()  
                                .startsWith(filterStr.toString().toUpperCase())) {  
                    filterDateList.add(sortModel);  
                }  
            }  
        }  
        // 根据a-z进行排序  
        Collections.sort(filterDateList, pinyinComparator);  
        adapter.updateListView(filterDateList);  
    }  
      
} 