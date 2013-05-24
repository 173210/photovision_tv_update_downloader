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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/

package photovision_tv_update_downloader;

import java.awt.Desktop;
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
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class Photovision_tv_update_downloader implements ModifyListener {
	protected Shell shlPhotovisionTvhw;
	private Text txtImei;
	private Text txtImsi;
	private Text txtIv;
	private Combo comboFw;
	private Table tblReleaseInfo;
	private Label lblReleaseInfo_disp;
	private Button btnGetReleaseInfo;
	private ReleaseInfo releaseinfo;
	private ArrayList<UpgradeInfo> listUpgradeInfo;
    private static Key key;
    private static IvParameterSpec iv_param_spec;

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

	public static void main(String[] args) {
		byte[] _keybuf = "HuaweiDeItmsIsVeryGood".getBytes();
        byte[] keybuf = new byte[24];
        for(int i = 0; i < _keybuf.length && i < 24; i++)
        	keybuf[i] = _keybuf[i];
        try {
        	key = SecretKeyFactory.getInstance("DESede").generateSecret(new DESedeKeySpec(keybuf));
        } catch (Exception e) {
        	e.printStackTrace();
        }

		try {
			Photovision_tv_update_downloader window = new Photovision_tv_update_downloader();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlPhotovisionTvhw.open();
		shlPhotovisionTvhw.layout();
		while (!shlPhotovisionTvhw.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	protected void createContents() {
		shlPhotovisionTvhw = new Shell();
		shlPhotovisionTvhw.setSize(650, 390);
		shlPhotovisionTvhw.setText("Photovision TV 202HW Update Downloader");
		shlPhotovisionTvhw.setLayout(new FormLayout());
		
		Label lblImei = new Label(shlPhotovisionTvhw, SWT.NONE);
		FormData fd_lblImei = new FormData();
		fd_lblImei.top = new FormAttachment(0, 10);
		fd_lblImei.left = new FormAttachment(0, 10);
		lblImei.setLayoutData(fd_lblImei);
		lblImei.setText("IMEI");
		
		txtImei = new Text(shlPhotovisionTvhw, SWT.BORDER);
		txtImei.addModifyListener(this);
		FormData fd_txtImei = new FormData();
		fd_txtImei.left = new FormAttachment(lblImei, 6);
		fd_txtImei.top = new FormAttachment(lblImei, 0, SWT.TOP);
		txtImei.setLayoutData(fd_txtImei);
		
		Button btnRandImei = new Button(shlPhotovisionTvhw, SWT.NONE);
		btnRandImei.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i;
				byte sum = 0;
				byte delta[] = {0, 1, 2, 3, 4, -4, -3, -2, -1, 0};
				StringBuilder strbuilder = (new StringBuilder()).append("86605201").append(String.format("%1$06d", new Random().nextInt(1000000)));
				for (i = 0; i < strbuilder.length(); i++ )
					sum += Integer.parseInt(strbuilder.substring(i, i + 1));
				for (i = strbuilder.length() - 1; i >= 0; i -= 2 )
					sum += delta[Integer.parseInt(strbuilder.substring(i, i + 1))];
				sum %= 10;
				txtImei.setText(strbuilder.append(sum == 0 ? 0 : 10 - sum).toString());
			}
		});
		fd_txtImei.right = new FormAttachment(btnRandImei, -6);
		FormData fd_btnRandImei = new FormData();
		fd_btnRandImei.top = new FormAttachment(lblImei, 0, SWT.TOP);
		fd_btnRandImei.right = new FormAttachment(100, -10);
		btnRandImei.setLayoutData(fd_btnRandImei);
		btnRandImei.setText("ランダム");
		
		Label lblImsi = new Label(shlPhotovisionTvhw, SWT.NONE);
		FormData fd_lblImsi = new FormData();
		fd_lblImsi.top = new FormAttachment(txtImei, 6);
		fd_lblImsi.left = new FormAttachment(0, 10);
		lblImsi.setLayoutData(fd_lblImsi);
		lblImsi.setText("IMSI");
		
		txtImsi = new Text(shlPhotovisionTvhw, SWT.BORDER);
		txtImsi.addModifyListener(this);
		FormData fd_txtImsi = new FormData();
		fd_txtImsi.right = new FormAttachment(txtImei, 0, SWT.RIGHT);
		fd_txtImsi.top = new FormAttachment(txtImei, 6);
		fd_txtImsi.left = new FormAttachment(lblImsi, 5);
		txtImsi.setLayoutData(fd_txtImsi);
		
		Button btnDefaultImsi = new Button(shlPhotovisionTvhw, SWT.NONE);
		btnDefaultImsi.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtImsi.setText("000000000000000");
			}
		});
		FormData fd_btnDefaultImsi = new FormData();
		fd_btnDefaultImsi.left = new FormAttachment(btnRandImei, 10, SWT.LEFT);
		fd_btnDefaultImsi.top = new FormAttachment(txtImei, 6);
		fd_btnDefaultImsi.right = new FormAttachment(btnRandImei, 0, SWT.RIGHT);
		btnDefaultImsi.setLayoutData(fd_btnDefaultImsi);
		btnDefaultImsi.setText("標準");
		
		Label lblFw = new Label(shlPhotovisionTvhw, SWT.NONE);
		FormData fd_lblFw = new FormData();
		fd_lblFw.top = new FormAttachment(txtImsi, 6);
		fd_lblFw.left = new FormAttachment(lblImei, 0, SWT.LEFT);
		lblFw.setLayoutData(fd_lblFw);
		lblFw.setText("元FW");

		comboFw = new Combo(shlPhotovisionTvhw, SWT.NONE);
		comboFw.addModifyListener(this);
		comboFw.setText("MMC392_V100R001C01B145_18.1");
		comboFw.add("MMC392_V100R001C01B145_18.1");
		comboFw.add("MMC392_V100R001C01B14a_19.1");
		comboFw.add("MMC392_V100R001C01B14d_21.1");
		FormData fd_comboFw = new FormData();
		fd_comboFw.right = new FormAttachment(txtImei, 0, SWT.RIGHT);
		fd_comboFw.top = new FormAttachment(txtImsi, 6);
		fd_comboFw.left = new FormAttachment(txtImei, 0, SWT.LEFT);
		comboFw.setLayoutData(fd_comboFw);
		
		Label lblIv = new Label(shlPhotovisionTvhw, SWT.NONE);
		FormData fd_lblIv = new FormData();
		fd_lblIv.top = new FormAttachment(comboFw, 6);
		fd_lblIv.left = new FormAttachment(0, 10);
		lblIv.setLayoutData(fd_lblIv);
		lblIv.setText("IV");
		
		txtIv = new Text(shlPhotovisionTvhw, SWT.BORDER);
		txtIv.addModifyListener(this);
		FormData fd_txtIv = new FormData();
		fd_txtIv.right = new FormAttachment(txtImei, 0, SWT.RIGHT);
		fd_txtIv.top = new FormAttachment(comboFw, 6);
		fd_txtIv.left = new FormAttachment(txtImei, 0, SWT.LEFT);
		txtIv.setLayoutData(fd_txtIv);
		
		Button btnRandIv = new Button(shlPhotovisionTvhw, SWT.NONE);
		btnRandIv.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				byte iv[] = new byte[8];
				new SecureRandom().nextBytes(iv);
				txtIv.setText(bufferToHex(iv, 0, 8));
			}
		});
		FormData fd_btnRandIv = new FormData();
		fd_btnRandIv.top = new FormAttachment(lblIv, -5, SWT.TOP);
		fd_btnRandIv.left = new FormAttachment(btnRandImei, 0, SWT.LEFT);
		btnRandIv.setLayoutData(fd_btnRandIv);
		btnRandIv.setText("ランダム");
		
		btnGetReleaseInfo = new Button(shlPhotovisionTvhw, SWT.NONE);
		btnGetReleaseInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getUpgradeInfo(txtImei.getText(), txtImsi.getText(), comboFw.getText(), txtIv.getText());
					lblReleaseInfo_disp.setText((new StringBuilder())
					.append("Ver:")
					.append(releaseinfo.Ver)
					.append(" VerID:")
					.append(releaseinfo.VerId)
					.append(" 状態:")
					.append(releaseinfo.Desc)
					.append(" 作成日時:")
					.append(releaseinfo.Createtime)
					.toString());
					tblReleaseInfo.removeAll();
					for (int i = 0; i < listUpgradeInfo.size(); i++) {
						UpgradeInfo upgradeinfo = listUpgradeInfo.get(i);
						String upgradeinfo_array[] = {upgradeinfo.OldVer, upgradeinfo.Size, upgradeinfo.MD5};
						new TableItem(tblReleaseInfo, SWT.NONE).setText(upgradeinfo_array);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnGetReleaseInfo.setEnabled(false);
		FormData fd_btnGetReleaseInfo = new FormData();
		fd_btnGetReleaseInfo.top = new FormAttachment(txtIv, 6);
		fd_btnGetReleaseInfo.right = new FormAttachment(100, -10);
		btnGetReleaseInfo.setLayoutData(fd_btnGetReleaseInfo);
		btnGetReleaseInfo.setText("情報取得");
		
		Label lblReleaseInfo = new Label(shlPhotovisionTvhw, SWT.NONE);
		FormData fd_lblReleaseInfo = new FormData();
		fd_lblReleaseInfo.top = new FormAttachment(btnGetReleaseInfo, 6);
		fd_lblReleaseInfo.right = new FormAttachment(btnRandImei, 0, SWT.RIGHT);
		fd_lblReleaseInfo.left = new FormAttachment(lblImei, 0, SWT.LEFT);
		lblReleaseInfo.setLayoutData(fd_lblReleaseInfo);
		lblReleaseInfo.setText("最新ファームウェア情報");
		
		lblReleaseInfo_disp = new Label(shlPhotovisionTvhw, SWT.NONE);
		FormData fd_lblReleaseInfo_disp = new FormData();
		fd_lblReleaseInfo_disp.top = new FormAttachment(lblReleaseInfo, 6);
		fd_lblReleaseInfo_disp.left = new FormAttachment(lblImei, 0, SWT.LEFT);
		fd_lblReleaseInfo_disp.right = new FormAttachment(btnRandImei, -10, SWT.RIGHT);
		lblReleaseInfo_disp.setLayoutData(fd_lblReleaseInfo_disp);
		
		Label lbltbl = new Label(shlPhotovisionTvhw, SWT.NONE);
		FormData fd_lbltbl = new FormData();
		fd_lbltbl.top = new FormAttachment(lblReleaseInfo_disp, 6);
		fd_lbltbl.left = new FormAttachment(lblImei, 0, SWT.LEFT);
		lbltbl.setLayoutData(fd_lbltbl);
		lbltbl.setText("一覧");
		
		Label lblCopyright = new Label(shlPhotovisionTvhw, SWT.NONE);
		FormData fd_lblCopyright = new FormData();
		fd_lblCopyright.bottom = new FormAttachment(100, -10);
		fd_lblCopyright.left = new FormAttachment(0, 10);
		lblCopyright.setLayoutData(fd_lblCopyright);
		lblCopyright.setText("このソフトウェアはGPLv3でライセンスされています。 Copyright © 2013 173210 All rights reserved.");
		
		final Button btnDownload = new Button(shlPhotovisionTvhw, SWT.NONE);
		btnDownload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Desktop.getDesktop().browse(
						new URI(listUpgradeInfo.get(tblReleaseInfo.getSelectionIndex()).Uri));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		btnDownload.setEnabled(false);
		FormData fd_btnDownload = new FormData();
		fd_btnDownload.bottom = new FormAttachment(lblCopyright, -6);
		fd_btnDownload.right = new FormAttachment(btnRandImei, 0, SWT.RIGHT);
		btnDownload.setLayoutData(fd_btnDownload);
		btnDownload.setText("ダウンロード");
		
		tblReleaseInfo = new Table(shlPhotovisionTvhw, SWT.BORDER | SWT.FULL_SELECTION);
		tblReleaseInfo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnDownload.setEnabled(true);
			}
		});
		FormData fd_tblReleaseInfo = new FormData();
		fd_tblReleaseInfo.bottom = new FormAttachment(btnDownload, -6);
		fd_tblReleaseInfo.top = new FormAttachment(lbltbl, 6);
		fd_tblReleaseInfo.right = new FormAttachment(btnRandImei, 0, SWT.RIGHT);
		fd_tblReleaseInfo.left = new FormAttachment(lblImei, 0, SWT.LEFT);
		tblReleaseInfo.setLayoutData(fd_tblReleaseInfo);
		tblReleaseInfo.setHeaderVisible(true);
		tblReleaseInfo.setLinesVisible(true);
		TableColumn tblcol = new TableColumn(tblReleaseInfo, SWT.LEFT);
		tblcol.setWidth(256);
		tblcol.setText("元ファームウェア");
		tblcol = new TableColumn(tblReleaseInfo, SWT.LEFT);
		tblcol.setWidth(96);
		tblcol.setText("容量 (B)");
		tblcol = new TableColumn(tblReleaseInfo, SWT.LEFT);
		tblcol.setWidth(256);
		tblcol.setText("MD5");

	}

	@Override
	public void modifyText(ModifyEvent arg0) {
	if (txtImei != null && txtImsi != null && comboFw != null && txtIv != null)
		btnGetReleaseInfo.setEnabled(
			txtImei.getText().length() == 15 &&
			txtImsi.getText().length() == 15 &&
			comboFw.getText().length() != 0 &&
			txtIv.getText().length() == 16);
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
        element.appendChild(createElement(doc, "IMEI", desEncrypt(imei)));
        element.appendChild(createElement(doc, "IMSI", desEncrypt(imsi)));
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

    public String desEncrypt(String src) throws Exception {
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
