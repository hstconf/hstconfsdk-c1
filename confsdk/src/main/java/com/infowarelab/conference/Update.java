package com.infowarelab.conference;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

////import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.infowarelabsdk.conference.util.NetUtil;

/**
 * 软件更新
 *
 * @author Sean.xie
 */
public class Update {


    public static final String SITE = "172.16.3.110";

    //public static final Logger log=Logger.getLogger(Update.class);

    /**
     * 检测更新
     *
     * @return
     * @throws Exception
     */
    public static boolean checkUpdate(ConferenceApplication confApp) throws Exception {
//		if(ConferenceApplication.Site_URL==null||ConferenceApplication.Site_URL.equals("")){
//			ConferenceApplication.Site_URL=Constants.DEFAULT_URL;
//		}
        StringBuilder url = new StringBuilder(SITE + "/meeting/remoteServlet?");
        url.append("funcName=getAndroidUpdate").append("&");
        url.append("clientVersion=").append(confApp.getVersion()).append("&");
        url.append("os=").append(confApp.getOs()).append("&");
        url.append("resolution=").append(confApp.getResolution());
        //log.info("Update"+url.toString());
        String response = NetUtil.doGet(url.toString());
        DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder dombuilder = domfac.newDocumentBuilder();
        Document doc = dombuilder.parse(new InputSource(new StringReader(response)));
        Element root = doc.getDocumentElement();
        NodeList elements = root.getElementsByTagName("return");
        if (elements.getLength() > 0) {
            Element element = (Element) elements.item(0);
            String returnValue = element.getFirstChild().getNodeValue();
            if ("0".equals(returnValue)) {
                confApp.setUpdate(Integer.valueOf(root.getElementsByTagName("Updatable").item(0).getFirstChild()
                        .getNodeValue()));
                if (confApp.getUpdate() != 0) {
                    confApp.setUpdateUrl(root.getElementsByTagName("updateUrl").item(0).getFirstChild().getNodeValue());
                    //log.info("update url"+root.getElementsByTagName("updateUrl").item(0).getFirstChild().getNodeValue());
                    return true;
                }
            }
        }
        return false;
    }

}
