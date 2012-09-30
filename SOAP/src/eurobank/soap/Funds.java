package eurobank.soap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import eurobank.soap.adapter.FundAdapter;
import eurobank.soap.model.Fund;
import eurobank.soap.model.FundUnit;

public class Funds extends Activity {
	
	private static final int MENU_ITEM_BUY = Menu.FIRST;
	private static final int MENU_ITEM_REFRESH = MENU_ITEM_BUY + 1;
	private static final int MENU_ITEM_WALLET = MENU_ITEM_REFRESH + 1;
	
	private BigDecimal money;
	private ListView walletListView;
	private List<Fund> fundItems;
	private FundAdapter fundAdapter;
	
	private static String SOAP_ACTION_BUY_FUND = "BuyFund";
	private static String SOAP_ACTION_FUNDS = "Funds";
	
	private static String NAMESPACE = "http://mycompany.com/eurobank/schemas";
	private static String METHOD_NAME_BUY_FUND = "BuyFundRequest";
	private static String METHOD_NAME_FUNDS = "FundsRequest";
	
	private static String URL = "http://192.168.10.111:23310/eurobank/FundService/";
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        walletListView = (ListView) findViewById(R.id.walletListView);

        fundItems = new ArrayList<Fund>();
        fundAdapter = new FundAdapter(this,
				R.layout.wallet_item, fundItems);
        
        walletListView.setAdapter(fundAdapter);
        registerForContextMenu(walletListView);
        
        getFunds();
		
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	super.onCreateOptionsMenu(menu);
		
    	MenuItem refreshMenuItem = menu.add(0, MENU_ITEM_REFRESH, Menu.NONE,
				R.string.refresh);
    	MenuItem walletMenuItem = menu.add(0, MENU_ITEM_WALLET, Menu.NONE,
				R.string.wallet);

		return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
			case (MENU_ITEM_REFRESH): {
				getFunds();
				return true;
			}
			case (MENU_ITEM_WALLET): {
				Intent intent = new Intent(this, Main.class);
				startActivity(intent);
				finish();
				return true;
			}
		}
		return false;
	}
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.setHeaderTitle(R.string.wallet_actions);
		menu.add(0, MENU_ITEM_BUY, Menu.NONE, R.string.buy);
	}
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		
		switch (item.getItemId()) {
			case (MENU_ITEM_BUY): {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int position = menuInfo.position;
				Fund fund = fundItems.get(position);
				buyFund(fund);
				getFunds();
				return true;
			}
		}
		
		return false;
	}
    
    private void getFunds() {
    	//Initialize soap request + add parameters
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME_FUNDS);
        
        PropertyInfo userInfo = new PropertyInfo();
        userInfo.setNamespace(NAMESPACE);
        userInfo.setName("UserName");
        userInfo.setValue("tomek");
        
        request.addProperty(userInfo);
        
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
     
        // Make the soap call.
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        try {
        	
        	//this is the actual part that will call the webservice
			androidHttpTransport.call(SOAP_ACTION_FUNDS, envelope);        
        } catch (Exception e) {
        	e.printStackTrace(); 
        }
        
		// Get the SoapResult from the envelope body.		
        SoapObject wallet;
		try {
			wallet = (SoapObject)envelope.bodyIn;
			fundItems.clear();
			if(wallet != null){
				for(int i=0; i < wallet.getPropertyCount(); i++) {
					if(i == 0) {
						SoapPrimitive moneyItem = (SoapPrimitive)wallet.getProperty(i);
						money = new BigDecimal(moneyItem.toString());
						TextView walletHeader = (TextView)findViewById(R.id.walletHeader);
						String walletTitle = getResources().getString(R.string.wallet);
						walletHeader.setText(walletTitle + " - " + money.toString());
					} else {
						SoapObject walletItem = (SoapObject)wallet.getProperty(i);
						long fundId = Long.valueOf(walletItem.getProperty("FundId").toString());
						String fundName = walletItem.getProperty("FundName").toString();
						String fundUnitName = walletItem.getProperty("FundUnitName").toString();
						BigDecimal price = new BigDecimal(walletItem.getProperty("Price").toString());
						Fund item = new Fund(fundId, fundName, FundUnit.valueOf(fundUnitName), price);
						fundItems.add(item);
					}
				}
				fundAdapter.notifyDataSetChanged();
				Toast.makeText(this, R.string.funds_refreshed, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
		}
    }
    
    private void buyFund(Fund fund) {
    	 //Initialize soap request + add parameters
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME_BUY_FUND);
        PropertyInfo fundInfo = new PropertyInfo();
        fundInfo.setNamespace(NAMESPACE);
        fundInfo.setName("FundId");
        fundInfo.setValue(Long.toString(fund.getFundId()));
        PropertyInfo unitInfo = new PropertyInfo();
        unitInfo.setNamespace(NAMESPACE);
        unitInfo.setName("FundUnitName");
        unitInfo.setValue(fund.getFundUnit().name());
        PropertyInfo userInfo = new PropertyInfo();
        userInfo.setNamespace(NAMESPACE);
        userInfo.setName("UserName");
        userInfo.setValue("tomek");
        PropertyInfo quantityInfo = new PropertyInfo();
        quantityInfo.setNamespace(NAMESPACE);
        quantityInfo.setName("Quantity");
        quantityInfo.setValue("1");
        request.addProperty(fundInfo);
        request.addProperty(unitInfo);
        request.addProperty(quantityInfo);
        request.addProperty(userInfo);
        
        
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
     
        // Make the soap call.
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        try {
        	
        	//this is the actual part that will call the webservice
			androidHttpTransport.call(SOAP_ACTION_BUY_FUND, envelope);        
        } catch (Exception e) {
        	e.printStackTrace(); 
        }
        
		// Get the SoapResult from the envelope body.		
        SoapPrimitive result;
		try {
			result = (SoapPrimitive)envelope.bodyIn;
			if(result != null){
				if(result.toString().equals("1")) {
					Toast.makeText(this, R.string.fund_unit_buy, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, R.string.not_enough_money, Toast.LENGTH_LONG).show();
				}
			}
		} catch (Exception e) {
		}
    }

}
