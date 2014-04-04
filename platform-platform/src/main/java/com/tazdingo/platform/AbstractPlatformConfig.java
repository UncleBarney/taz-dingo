/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tazdingo.platform;

import com.tazdingo.http.IPlatform;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Cynthia
 */
public abstract class AbstractPlatformConfig {

    /**
     *
     * @return
     */
    public IPlatform defaultPlatformConfiguration() {
        IPlatform platform = null;
        try {
            File fXmlFile = new File("./src/platform.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList nlist = doc.getElementsByTagName("platform");

            if (nlist.getLength() > 0) {
                Node node = nlist.item(0);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String platformname = element.getElementsByTagName("name").item(0).getTextContent();
                    String defaultkeyserverurl = element.getElementsByTagName("defaultkeyserverurl").item(0).getTextContent();
                    String defaultkeyservername = element.getElementsByTagName("defaultkeyservername").item(0).getTextContent();
                    Scanner scanIn = new Scanner(System.in);
                    String password = null;
                    System.out.println("Enter platform adminpassword:");
                    String adminpassword = scanIn.next();
                    NodeList keylist = doc.getElementsByTagName("key");
                    scanIn.close();
                    if (keylist.getLength() == 0) {
                        System.out.println("Enter password:");
                        password = scanIn.next();
                        String encryptedpassword = Encryption.encrypt(Encryption.generateSecretKey(adminpassword), password);
                        Element key = doc.createElement("key");
                        key.appendChild(doc.createTextNode(encryptedpassword));
                        node.appendChild(key);
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(new File("./src/platform.xml"));
                        transformer.transform(source, result);
                    } else {
                        String encryptedpassword = keylist.item(0).getTextContent();
                        password = Encryption.decrypt(Encryption.generateSecretKey(adminpassword), encryptedpassword);
                    }
                    if (!password.equals(ConstantUtil.WRONDKEY)) {
                        platform = newPlatform(platformname, password, adminpassword, defaultkeyserverurl, defaultkeyservername);
                        NodeList keyserverlist = doc.getElementsByTagName("keyserver");
                        for (int i = 0; i < keyserverlist.getLength(); i++) {
                            Node keyserverNode = keyserverlist.item(i);
                            if (keyserverNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element keyserver = (Element) keyserverNode;
                                String keyservername = keyserver.getElementsByTagName("name").item(0).getTextContent();
                                String keyserverurl = keyserver.getElementsByTagName("url").item(0).getTextContent();
                                String privilege = keyserver.getElementsByTagName("privilege").item(0).getTextContent();
                                platform.addKeyServer(keyservername, keyserverurl, privilege);
                            }

                        }
                        NodeList servicelist = doc.getElementsByTagName("service");
                        for (int i = 0; i < servicelist.getLength(); i++) {
                            Node serviceNode = servicelist.item(i);
                            if (serviceNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element service = (Element) serviceNode;
                                String servicename = service.getElementsByTagName("name").item(0).getTextContent();
                                String serviceurl = service.getElementsByTagName("url").item(0).getTextContent();
                                String privilege = service.getElementsByTagName("privilege").item(0).getTextContent();
                                if (service.getElementsByTagName("keyservername").getLength() == 0) {
                                    platform.addService(servicename, serviceurl, privilege);
                                } else {
                                    String keyservername = service.getElementsByTagName("keyservername").item(0).getTextContent();
                                    platform.addService(servicename, serviceurl, privilege, keyservername);
                                }
                            }

                        }
                        NodeList devicelist = doc.getElementsByTagName("device");
                        for (int i = 0; i < devicelist.getLength(); i++) {
                            Node deviceNode = devicelist.item(i);
                            if (deviceNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element device = (Element) deviceNode;
                                String deviceid = device.getElementsByTagName("id").item(0).getTextContent();
                                String privilege = device.getElementsByTagName("privilege").item(0).getTextContent();
                                if (device.getElementsByTagName("keyservername").getLength() == 0) {
                                    platform.addDevice(deviceid, privilege);
                                } else {
                                    String keyservername = device.getElementsByTagName("keyservername").item(0).getTextContent();
                                    platform.addDevice(deviceid, privilege, keyservername);
                                }
                            }
                        }

                        NodeList blocklist = doc.getElementsByTagName("blockeddeviceid");
                        for (int i = 0; i < blocklist.getLength(); i++) {
                            String deviceid = blocklist.item(i).getTextContent();
                            platform.blockDevice(deviceid);
                        }

                        NodeList userlist = doc.getElementsByTagName("user");
                        for (int i = 0; i < userlist.getLength(); i++) {
                            Node userNode = userlist.item(i);
                            if (userNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element user = (Element) userNode;
                                String username = user.getElementsByTagName("name").item(0).getTextContent();
                                String privilege = user.getElementsByTagName("privilege").item(0).getTextContent();
                                if (user.getElementsByTagName("keyservername").getLength() == 0) {
                                    platform.addUser(username, privilege);
                                } else {
                                    String keyservername = user.getElementsByTagName("keyservername").item(0).getTextContent();
                                    platform.addUser(username, privilege, keyservername);
                                }
                            }
                        }

                    }

                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(AbstractPlatformConfig.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(AbstractPlatformConfig.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(AbstractPlatformConfig.class.getName()).log(Level.SEVERE, null, ex);
        }

        return platform;

    }

    protected abstract IPlatform newPlatform(String platformname, String password, String adminpassword, String defaultkeyserverurl, String defaultkeyservername);
}
