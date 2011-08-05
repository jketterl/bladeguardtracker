package net.djmacgyver.bgt.map;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.djmacgyver.bgt.R;
import net.djmacgyver.bgt.http.HttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class MapList implements ListAdapter {
	private Context context;
	private HttpClient client;
	private Document dom;
	private NodeList maps;
	
	public MapList(Context context) {
		this.context = context;
	}
	
	private HttpClient getClient() {
		if (client == null) {
			return new HttpClient(context);
		}
		return client;
	}
	
	private Document getDom() {
		if (dom == null) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			
			HttpGet req = new HttpGet(context.getResources().getString(R.string.base_url) + "map");
			try {
				HttpEntity e = getClient().execute(req).getEntity();
				DocumentBuilder builder = dbf.newDocumentBuilder();
				dom = builder.parse(e.getContent());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return dom;
	}
	
	private NodeList getMaps() {
		if (maps == null) {
			XPath x = XPathFactory.newInstance().newXPath();
			try {
				XPathExpression mapExpr = x.compile("/maps/map");
				maps = (NodeList) mapExpr.evaluate(getDom(), XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return maps;
	}

	@Override
	public int getCount() {
		return getMaps().getLength();
	}

	@Override
	public Object getItem(int arg0) {
		return getMaps().item(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return Integer.parseInt(getMaps().item(arg0).getAttributes().getNamedItem("id").getNodeValue());
	}

	@Override
	public int getItemViewType(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		LayoutInflater inf = LayoutInflater.from(context);
		View v = inf.inflate(R.layout.maplistitem, arg2, false);
		TextView text = (TextView) v.findViewById(R.id.mapName);
		Node map = getMaps().item(arg0);
		text.setText(map.getTextContent());
		return v;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return getCount() <= 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int arg0) {
		return true;
	}

}
