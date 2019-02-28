package yusq.hyperledger.example.demo;

import yusq.hyperledger.example.AppUser;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;

import java.io.*;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import static java.lang.String.format;

/**
 * @Author: yusq
 * @Date: 2018/12/26 0026
 */
public class FabricConfig {
    private static final String PATH = System.getProperty("user.dir");
    private static final String ORDERERSERVERPATH = PATH+"\\basic-network\\crypto-config\\ordererOrganizations\\example.com\\orderers\\orderer.example.com\\tls";
    private static final String PEERSERVERPATH = PATH+"\\basic-network\\crypto-config\\peerOrganizations\\org2.example.com\\peers\\peer0.org2.example.com\\tls";
    private static final String ORDERERPATH = PATH+"\\basic-network\\crypto-config\\ordererOrganizations\\example.com\\users\\Admin@example.com\\tls";
     private static final String PEERPATH = PATH+"\\basic-network\\crypto-config\\peerOrganizations\\org2.example.com\\users\\Admin@org2.example.com\\tls";
    /**
     * 获取orderer的配置
     * @param name
     * @return
     */
    public Properties getOrdererProperties(String name) {

        return getEndPointProperties("orderer", name);

    }

    /**
     * 获取peer的配置
     * @param name
     * @return
     */
    public Properties getPeerProperties(String name) {

        return getEndPointProperties("peer", name);

    }
    public boolean isRunningAgainstFabric10() {

        return "IntegrationSuiteV1.java".equals(System.getProperty("org.hyperledger.fabric.sdktest.ITSuite"));

    }

    /**
     * 获取配置的公共方法
     * @param type
     * @param name
     * @return
     */
    public Properties getEndPointProperties(final String type, final String name) {
        Properties ret = new Properties();

        String pathserver = type =="orderer"?ORDERERSERVERPATH:PEERSERVERPATH;
        String path = type =="orderer"?ORDERERPATH:PEERPATH;
        File cert = Paths.get(pathserver+"/server.crt").toFile();
        if (!cert.exists()) {
            throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", name,
                    cert.getAbsolutePath()));
        }

        if (!isRunningAgainstFabric10()) {
            File clientCert;
            File clientKey;
            if ("orderer".equals(type)) {
                clientCert = Paths.get(path+"/client.crt").toFile();
                clientKey = Paths.get(path+"/client.key").toFile();
            } else {
                clientCert = Paths.get(path+"/client.crt").toFile();
                clientKey = Paths.get(path+"/client.key").toFile();
            }

            if (!clientCert.exists()) {
                throw new RuntimeException(String.format("Missing  client cert file for: %s. Could not find at location: %s", name,
                        clientCert.getAbsolutePath()));
            }

            if (!clientKey.exists()) {
                throw new RuntimeException(String.format("Missing  client key file for: %s. Could not find at location: %s", name,
                        clientKey.getAbsolutePath()));
            }
            ret.setProperty("clientCertFile", clientCert.getAbsolutePath());
            ret.setProperty("clientKeyFile", clientKey.getAbsolutePath());
        }

        ret.setProperty("pemFile", cert.getAbsolutePath());
        ret.setProperty("hostnameOverride", name);
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("negotiationType", "TLS");
        try {
            ret.store(new FileWriter(PATH + name + ".properties"), "文件自述");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 读取用户
     * @param name
     * @param org
     * @param mspId
     * @param privateKeyFile
     * @param certificateFile
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public AppUser getMember(String name, String org, String mspId, File privateKeyFile,
                             File certificateFile) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {

        AppUser appUser = new AppUser();
        appUser.setName(name);
        appUser.setMspId(mspId);
        appUser.setAffiliation(org);

        String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");
        PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));
        appUser.setEnrollment(new SampleStoreEnrollement(privateKey, certificate));

        return appUser;
    }

    /**
     * 转换私钥
     * @param data
     * @return
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    static PrivateKey getPrivateKeyFromBytes(byte[] data) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        final Reader pemReader = new StringReader(new String(data));

        final PrivateKeyInfo pemPair;
        try (PEMParser pemParser = new PEMParser(pemReader)) {
            pemPair = (PrivateKeyInfo) pemParser.readObject();
        }
        Security.addProvider(new BouncyCastleProvider());
        PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);

        return privateKey;
    }

    /**
     * c查找私钥
     * @param directory
     * @return
     */
    public static File findFileSk(File directory) {

        File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));

        if (null == matches) {
            throw new RuntimeException(format("Matches returned null does %s directory exist?", directory.getAbsoluteFile().getName()));
        }

        if (matches.length != 1) {
            throw new RuntimeException(format("Expected in %s only 1 sk file but found %d", directory.getAbsoluteFile().getName(), matches.length));
        }

        return matches[0];

    }

    static final class SampleStoreEnrollement implements Enrollment, Serializable {

        private static final long serialVersionUID = -2784835212445309006L;
        private final PrivateKey privateKey;
        private final String certificate;

        SampleStoreEnrollement(PrivateKey privateKey, String certificate) {

            this.certificate = certificate;

            this.privateKey = privateKey;
        }

        @Override
        public PrivateKey getKey() {

            return privateKey;
        }

        @Override
        public String getCert() {
            return certificate;
        }

    }
}
