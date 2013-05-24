/* 
 * Copyright (C) 2013 173210 <root.3.173210@live.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

import java.awt.Button;
import java.awt.Desktop;
import java.awt.Label;
import java.awt.List;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.Key;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


public class photovision_tv_update_downloader extends JApplet implements TextListener {
	private TextField txtFdImei;
	private TextField txtFdImsi;
	private TextField txtFdIv;
	private Button btnGetinfo;
	private Button btndownload;
	private ReleaseInfo releaseinfo;
	private ArrayList<UpgradeInfo> listUpgradeInfo;
	private byte Key_byte[];
    private Key key;
    private IvParameterSpec iv_param_spec;

    private class ReleaseInfo {

    	private String Ver;
    	private String VerId;
    	private String Desc;
    	private String Createtime;
    	private String Url;
    }

    private class UpgradeInfo {

    	private String MD5;
    	private String OldVer;
    	private String Size;
    	private String Uri;
    }

	public photovision_tv_update_downloader() {
		getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(1dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.GROWING_BUTTON_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.BUTTON_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("fill:max(1dlu;pref):grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		Label lblImei = new Label("IMEI");
		getContentPane().add(lblImei, "2, 2");
		
		txtFdImei = new TextField();
		txtFdImei.addTextListener(this);
		getContentPane().add(txtFdImei, "4, 2");
		
		Button btnRandImei = new Button("ランダム");
		btnRandImei.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i, sum = 0;
				int delta[] = {0, 1, 2, 3, 4, -4, -3, -2, -1, 0};
				StringBuilder strbuilder = (new StringBuilder()).append("86605201").append(String.format("%1$06d", new Random().nextInt(1000000)));
			    for (i = 0; i < strbuilder.length(); i++ )
					sum += Integer.parseInt(strbuilder.substring(i, i + 1));
				for (i = strbuilder.length() - 1; i >= 0; i -= 2 )
					sum += delta[Integer.parseInt(strbuilder.substring(i, i + 1))];
				sum %= 10;
				txtFdImei.setText(strbuilder.append(sum == 0 ? 0 : 10 - sum).toString());

			}
		});
		getContentPane().add(btnRandImei, "6, 2");
		
		Label lblImsi = new Label("IMSI");
		getContentPane().add(lblImsi, "2, 4");
		
		txtFdImsi = new TextField();
		txtFdImsi.addTextListener(this);
		getContentPane().add(txtFdImsi, "4, 4, default, top");
		
		Button btnDefaultImsi = new Button("標準");
		btnDefaultImsi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtFdImsi.setText("000000000000000");
			}
		});
		getContentPane().add(btnDefaultImsi, "6, 4");
		
		Label lblFw = new Label("元ファームウェア");
		getContentPane().add(lblFw, "2, 6");
		
		String listFw[] = {
				"MMC392_V100R001C01B145_18.1",
				"MMC392_V100R001C01B14a_19.1",
				"MMC392_V100R001C01B14d_21.1"};
		final JComboBox<String> comboBoxFw = new JComboBox<String>(listFw);
		comboBoxFw.setEditable(true);
		getContentPane().add(comboBoxFw, "4, 6, fill, default");
		
		Label lblIv = new Label("IV");
		getContentPane().add(lblIv, "2, 8");
		
		txtFdIv = new TextField();
		txtFdIv.addTextListener(this);
		getContentPane().add(txtFdIv, "4, 8");
		
		Button btnRandIv = new Button("ランダム");
		btnRandIv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	            byte iv[] = new byte[8];
	            new SecureRandom().nextBytes(iv);
	            txtFdIv.setText(bufferToHex(iv, 0, 8));
			}
		});
		getContentPane().add(btnRandIv, "6, 8");
		
		Label lblReleaseInfo = new Label("ファームウェア情報");
		getContentPane().add(lblReleaseInfo, "2, 12");
		
		final Label ReleaseInfo_disp = new Label("");
		getContentPane().add(ReleaseInfo_disp, "2, 13, 5, 1");
		
		Label lbllist = new Label("一覧");
		getContentPane().add(lbllist, "2, 15");
		
		final List list = new List();
		list.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				btndownload.setEnabled(true);
			}
		});
		getContentPane().add(list, "2, 16, 5, 1");
		
		btnGetinfo = new Button("情報取得");
		btnGetinfo.setEnabled(false);
		getContentPane().add(btnGetinfo, "6, 10");
		btnGetinfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					getUpgradeInfo(txtFdImei.getText(), txtFdImsi.getText(), (String)comboBoxFw.getSelectedItem(), txtFdIv.getText());
					ReleaseInfo_disp.setText((new StringBuilder())
							.append("バージョン:")
							.append(releaseinfo.Ver)
							.append(" バージョンID:")
							.append(releaseinfo.VerId)
							.append(" 状態:")
							.append(releaseinfo.Desc)
							.append(" 作成日時:")
							.append(releaseinfo.Createtime)
							.toString());
					list.removeAll();
			        for (int i = 0; i < listUpgradeInfo.size(); i++) {
			        	UpgradeInfo upgradeinfo = listUpgradeInfo.get(i);
			            list.add((new StringBuilder())
			            		.append("元FW:")
			            		.append(upgradeinfo.OldVer)
			            		.append(" 容量:")
			            		.append(upgradeinfo.Size)
			            		.append("B MD5:")
			            		.append(upgradeinfo.MD5)
			            		.toString());
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		btndownload = new Button("ダウンロード");
		btndownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
		        try {
		        	Desktop.getDesktop().browse(
		        			new URI(listUpgradeInfo.get(list.getSelectedIndex()).Uri));
		        } catch (Exception e) {
		        	e.printStackTrace();
		        }
			}
		});
		btndownload.setEnabled(false);
		getContentPane().add(btndownload, "6, 18");
		
		JLabel lblCopyright = new JLabel("このソフトウェアはGPLv3でライセンスされています。 Copyright © 2013 173210 All rights Reserved.");
		getContentPane().add(lblCopyright, "2, 20, 5, 1");

	}
	
	public void init() {
		byte[] _keybuf = "HuaweiDeItmsIsVeryGood".getBytes();
        byte[] keybuf = new byte[24];
        for(int i = 0; i < _keybuf.length && i < 24; i++)
        	keybuf[i] = _keybuf[i];
        try {
        	key = SecretKeyFactory.getInstance("DESede").generateSecret(new DESedeKeySpec(keybuf));
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

    private void getUpgradeInfo(String imei, String imsi, String firmware, String iv)
    		throws Exception
    {
		HttpURLConnection connection =
			(HttpURLConnection)new URL("http://update-japan.huaweidevice.com:80/PhotoFrame_Softbank/UrlCommand/CheckNewVersion.aspx").openConnection();
		OutputStreamWriter osw;

		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.connect();
		osw = new OutputStreamWriter(connection.getOutputStream());
		osw.write(getDeviceXmlContent(imei, imsi, firmware, iv));
		osw.flush();
		osw.close();
       	releaseinfo = parseRuleXml(connection.getInputStream());
       	connection.disconnect();
        connection = (HttpURLConnection)new URL((new StringBuilder()).append(releaseinfo.Url).append("hota.xml").toString()).openConnection();
        listUpgradeInfo = parseFotaXml(connection.getInputStream());
        connection.disconnect();
    }

	@Override
	public void textValueChanged(TextEvent e) {
		btnGetinfo.setEnabled(
				txtFdImei.getText().length() == 15 &&
				txtFdImsi.getText().length() == 15 &&
				txtFdIv.getText().length() == 16);
	}

    private ReleaseInfo parseRuleXml(InputStream xml) throws Exception {
    	int len;
    	ReleaseInfo ret = new ReleaseInfo();
        NodeList nlcomponents = DocumentBuilderFactory
        						.newInstance()
        						.newDocumentBuilder()
        						.parse(xml)
        						.getDocumentElement()
        						.getElementsByTagName("components");
    	NodeList nlcomponent;
        if(nlcomponents == null || nlcomponents.getLength() <= 0) return null;
        len = nlcomponents.getLength();
        for (int i = 0; i < len; i++) {
        	nlcomponent = ((Element)nlcomponents.item(i)).getElementsByTagName("component");
        	if(nlcomponent == null || nlcomponent.getLength() <= 0) continue;
        	NodeList nl = ((Element)nlcomponent.item(0)).getElementsByTagName("version");
        	if(nl != null && nl.getLength() > 0)
        		ret.Ver = nl.item(0).getFirstChild().getNodeValue();
        	nl = ((Element)nlcomponent.item(0)).getElementsByTagName("versionID");
        	if(nl != null && nl.getLength() > 0)
        		ret.VerId = nl.item(0).getFirstChild().getNodeValue();
        	nl = ((Element)nlcomponent.item(0)).getElementsByTagName("description");
        	if(nl != null && nl.getLength() > 0)
        		ret.Desc = nl.item(0).getFirstChild().getNodeValue();
        	nl = ((Element)nlcomponent.item(0)).getElementsByTagName("createtime");
        	if(nl != null && nl.getLength() > 0)
        		ret.Createtime = nl.item(0).getFirstChild().getNodeValue();
        	nl = ((Element)nlcomponent.item(0)).getElementsByTagName("url");
        	if(nl != null && nl.getLength() > 0)
        		ret.Url = nl.item(0).getFirstChild().getNodeValue();
        	return ret;
        }
        return null;
    }

	private String getDeviceXmlContent(String imei, String imsi, String firmware, String iv)
			throws Exception
	{
		byte iv_buf[] = new byte[8];
	    for (int index = 0; index < 8; index++) {
	        iv_buf[index] = (byte) Integer.parseInt(iv.substring(index * 2, (index + 1) * 2), 16);
	    }
		iv_param_spec = new IvParameterSpec(iv_buf);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element element = doc.createElement("root");
        doc.appendChild(element);
        element.appendChild(createElement(doc, "DeviceName", "MMC392"));
        element.appendChild(createElement(doc, "IMEI", desEncrypt(imei, Key_byte)));
        element.appendChild(createElement(doc, "IMSI", desEncrypt(imsi, Key_byte)));
        element.appendChild(createElement(doc, "FirmWare", firmware));
        return docToString(doc);
	}

    private ArrayList<UpgradeInfo> parseFotaXml(InputStream xml) throws Exception {
        Element element;
        UpgradeInfo upgradeinfo;
        ArrayList<UpgradeInfo> listUpgradeInfo = new ArrayList<UpgradeInfo>();
        element = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(xml)
				.getDocumentElement();
        NodeList nodelist = element.getElementsByTagName("component");
        if(nodelist == null) return null;
        int len = nodelist.getLength();
       	if(len <= 0) return null;
       	for(int i = 0; i < len; i++) {
       		upgradeinfo = new UpgradeInfo();
       		Element element1 = (Element)nodelist.item(i);
       		upgradeinfo.OldVer = element1.getAttribute("oldVer");
   			upgradeinfo.Size = element1.getAttribute("size");
   			upgradeinfo.MD5 = element1.getAttribute("md5");
   			upgradeinfo.Uri = (new StringBuilder()).append(releaseinfo.Url).append(element1.getAttribute("uri")).toString();
   			listUpgradeInfo.add(upgradeinfo);
       	}
       	return listUpgradeInfo;
    }

    private Element createElement(Document doc, String name, String textObj) {
        Element element = doc.createElement("rule");
        element.setAttribute("name", name);
        element.appendChild(doc.createTextNode(textObj));
        return element;
    }

    public static String bufferToHex(byte bin[], int start, int len) {
        char hex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder strbuilder = new StringBuilder();
        for(int i = start; i < start + len; i++) {
            byte byte0 = bin[i];
            strbuilder.append(hex[0xf & byte0 >>> 4]);
            strbuilder.append(hex[byte0 & 0xf]);
        }

        return strbuilder.toString();
    }

    public byte[] encrypt(byte src[]) throws Exception {
        if(src == null) return null;
        int i = src.length % 8;
        if(i != 0) {
            byte _src[] = src;
            src = new byte[_src.length + (8 - i)];
            for(i = 0; i < _src.length; i++)
                src[i] = _src[i];
        }
        Cipher cipher;
		cipher = Cipher.getInstance("DESede/CBC/NoPadding");
		cipher.init(1, key, iv_param_spec);
		return cipher.doFinal(src);
    }

    public String desEncrypt(String src, byte keybuf[]) throws Exception {
        if(src == null) return null;
        byte iv_byte[] = iv_param_spec.getIV();
        String s1 = (new StringBuilder()).append("HEX:").append(bufferToHex(iv_byte, 0, iv_byte.length)).toString();
        byte enc[] = encrypt(src.getBytes());
        return (new StringBuilder()).append(s1).append(bufferToHex(enc, 0, enc.length)).toString();
    }

    private String docToString(Document doc) throws Exception {
        ByteArrayOutputStream strm = new ByteArrayOutputStream();
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(strm));
        String ret = strm.toString();
        if(strm != null)
        	strm.close();
        return ret;
    }
}