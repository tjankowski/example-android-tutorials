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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import eurobank.soap.R;

import eurobank.soap.adapter.WalletItemAdapter;
import eurobank.soap.model.FundUnit;
import eurobank.soap.model.WalletItem;

public class Main extends Activity {
	
	private static final int MENU_ITEM_SELL = Menu.FIRST;
	private static final int MENU_ITEM_REFRESH = MENU_ITEM_SELL + 1;
	private static final int MENU_ITEM_FUNDS = MENU_ITEM_REFRESH + 1;
	
	private BigDecimal money;
	private ListView walletListView;
	private List<WalletItem> walletItems;
	private WalletItemAdapter walletItemsAdapter;
	
	private static String SOAP_ACTION_SELL_FUND = "SellFund";
	private static String SOAP_ACTION_WALLET = "Wallet";
	
	private static String NAMESPACE = "http://mycompany.com/eurobank/schemas";
	private static String METHOD_NAME_SELL_FUND = "SellFundRequest";
	private static String METHOD_NAME_WALLET = "WalletRequest";
	
	private static String URL = "http://192.168.10.111:23310/eurobank/FundService/";
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        walletListView = (ListView) findViewById(R.id.walletListView);

        walletItems = new ArrayList<WalletItem>();
        walletItemsAdapter = new WalletItemAdapter(this,
				R.layout.wallet_item, walletItems);
        
        walletListView.setAdapter(walletItemsAdapter);
        registerForContextMenu(walletListView);
        
        getWalletItems();
		
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// TODO Auto-generated method stub
    	super.onCreateOptionsMenu(menu);
		
    	MenuItem refreshMenuItem = menu.add(0, MENU_ITEM_REFRESH, Menu.NONE,
				R.string.refresh);
    	MenuItem fundsMenuItem = menu.add(0, MENU_ITEM_FUNDS, Menu.NONE,
				R.string.funds);

		return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
			case (MENU_ITEM_REFRESH): {
				getWalletItems();
				return true;
			}
			case (MENU_ITEM_FUNDS): {
				Intent intent = new Intent(this, Funds.class);
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
		menu.add(0, MENU_ITEM_SELL, Menu.NONE, R.string.sell);
	}
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		
		switch (item.getItemId()) {
			case (MENU_ITEM_SELL): {
				AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
				int position = menuInfo.position;
				WalletItem walletItem = walletItems.get(position);
				sellFund(walletItem);
				getWalletItems();
				return true;
			}
			
		}
		
		return false;
	}
    
    private void getWalletItems() {
    	//Initialize soap request + add parameters
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME_WALLET);
        
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
			androidHttpTransport.call(SOAP_ACTION_WALLET, envelope);        
        } catch (Exception e) {
        	e.printStackTrace(); 
        }
        
		// Get the SoapResult from the envelope body.		
        SoapObject wallet;
		try {
			wallet = (SoapObject)envelope.bodyIn;
			walletItems.clear();
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
						int quantity = Integer.valueOf(walletItem.getProperty("Quantity").toString());
						WalletItem item = new WalletItem(fundId, fundName, FundUnit.valueOf(fundUnitName), quantity);
						walletItems.add(item);
					}
				}
				walletItemsAdapter.notifyDataSetChanged();
				Toast.makeText(this, R.string.wallet_refreshed, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    private void sellFund(WalletItem item) {
   	 //Initialize soap request + add parameters
       SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME_SELL_FUND);
       PropertyInfo fundInfo = new PropertyInfo();
       fundInfo.setNamespace(NAMESPACE);
       fundInfo.setName("FundId");
       fundInfo.setValue(item.getFundId());
       PropertyInfo unitInfo = new PropertyInfo();
       unitInfo.setNamespace(NAMESPACE);
       unitInfo.setName("FundUnitName");
       unitInfo.setValue(item.getFundUnit().name());
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
			androidHttpTransport.call(SOAP_ACTION_SELL_FUND, envelope);        
       } catch (Exception e) {
       	e.printStackTrace(); 
       }
       
		// Get the SoapResult from the envelope body.		
       SoapPrimitive result;
		try {
			result = (SoapPrimitive)envelope.bodyIn;
			if(result != null){
				if(result.toString().equals("1")) {
					Toast.makeText(this, R.string.fund_unit_sold, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, "Error: " + result.toString(), Toast.LENGTH_LONG).show();	
				}
			}
		} catch (Exception e) {
		}
   }
}