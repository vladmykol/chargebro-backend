package com.vladmykol.takeandcharge.service;

//@Service
//@RequiredArgsConstructor
//@Slf4j
public class LiqPayService {
//    private final LiqPayHistoryRepository liqPayHistoryRepository;
//    private final UserWalletRepository userWalletRepository;
//    private final UserRepository userRepository;
//    @Value("${takeandcharge.pay.link}")
//    private String checkoutLink;
//    @Value("${takeandcharge.pay.pass}")
//    private String privateKey;
//    @Value("${takeandcharge.pay.user}")
//    private String publicKey;
//    @Value("${takeandcharge.api.domain}")
//    private String callbackUri;

//    public void savePaymentCallback(LiqPayHistory liqPayHistory) {
//
//    }

//    public List<LiqPayHistory> getAllPaymentHistory() {
//        return liqPayHistoryRepository.findAll();
//    }
//
//    public String prepareCheckoutUrl(String username) {
//        Map<String, String> params = new HashMap<>();
//        params.put("action", "auth");
//        params.put("amount", "1");
//        params.put("currency", "UAH");
//        params.put("result_url", callbackUri);
//        params.put("description", "Authorization");
//        params.put("customer", username);
//        params.put("language", "ua");
//        params.put("recurringbytoken", "1");
//        params.put("server_url", callbackUri + "/pay/callback");
//        params.put("sandbox", "1"); // enable the testing environment and card will NOT charged. If not set will be used property isCnbSandbox()
//        LiqPay liqpay = new LiqPay(publicKey, privateKey);
//        String html = liqpay.cnb_form(params);
//        var data = StringUtils.substringBetween(html, "name=\"data\" value=\"", "\" />");
//        var signature = StringUtils.substringBetween(html, "name=\"signature\" value=\"", "\" />");
//
//        return String.format(checkoutLink, data, signature);
//    }
//
//    public void callback(String data, String signature) throws JsonProcessingException {
//        var sign = LiqPayUtil.base64_encode(LiqPayUtil.sha1(privateKey +
//                data +
//                privateKey));
//        if (!sign.equals(signature)) {
//            throw new BadCredentialsException("request signature is invalid");
//        }
//
//        var jsonData = new String(new Base64().decode(data));
//        LiqPayHistory liqPayCallback;
//
//        try {
//            liqPayCallback = new ObjectMapper().readValue(jsonData, LiqPayHistory.class);
//            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(liqPayCallback.getCustomer(), null);
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            savePaymentCallback(liqPayCallback);
//        } catch (IOException e) {
//            liqPayCallback = new LiqPayHistory();
//            liqPayCallback.setStatus("Fail to parse data");
//            liqPayCallback.setAction(jsonData);
//            savePaymentCallback(liqPayCallback);
//            throw e;
//        }
//    }
}
