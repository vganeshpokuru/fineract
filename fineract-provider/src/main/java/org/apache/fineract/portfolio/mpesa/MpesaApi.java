package org.apache.fineract.portfolio.mpesa;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@SuppressWarnings("deprecation")
@Path("/mpesatransactions")
@Component
public class MpesaApi {

	private final JdbcTemplate jdbcTemplate;
	private final Set<String> PARAMETERS = new HashSet<>(Arrays.asList());
	private BankPropertyReadPlatfomService bankPropertyReadPlatfomService;
	private final FromJsonHelper fromJsonHelper;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<Object> toApiJsonSerializer;
	private final PlatformSecurityContext context;
	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public MpesaApi(BankPropertyReadPlatfomService bankPropertyReadPlatfomService, FromJsonHelper fromJsonHelper,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final DefaultToApiJsonSerializer<Object> toApiJsonSerializer, final PlatformSecurityContext context,
			final RoutingDataSource dataSource, final FromJsonHelper fromApiJsonfromApiJsonHelper) {
		this.bankPropertyReadPlatfomService = bankPropertyReadPlatfomService;
		this.fromJsonHelper = fromJsonHelper;
		this.context = context;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.fromApiJsonHelper = fromApiJsonfromApiJsonHelper;
	}

	@POST
	@Path("/verify")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String verify(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
		final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
		final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
		BankWalletNumberVerificationData bankWalletNumberVerificationData = new BankWalletNumberVerificationData(false);
		BankProperty bankProperty = bankPropertyReadPlatfomService
				.retrieveBankPropertDescription(query.stringValueOfParameterNamed("walletNumber"));
		if (bankProperty.getPropertyValue().equals(query.stringValueOfParameterNamed("walletNumber"))) {
			bankWalletNumberVerificationData.setIsVerified(true);
		}
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, bankWalletNumberVerificationData, PARAMETERS);
	}

	@SuppressWarnings("resource")
	@POST
	@Path("/ministatement")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String miniStatement(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo)
			throws NoSuchAlgorithmException, ClientProtocolException, IOException, JSONException {
		BankProperty timeZone = bankPropertyReadPlatfomService.retrieveBankPropertName("time_zone");
		TimeZone lagosTimeZone = TimeZone.getTimeZone(timeZone.getPropertyValue());
		Calendar calendar = Calendar.getInstance(lagosTimeZone);
		Long timestamp = calendar.getTimeInMillis() / 1000;
		String timeStampString = timestamp.toString();
		UUID uuid = UUID.randomUUID();
		String nonce = uuid.toString().replaceAll("-", "");
		BankProperty signatureMethod = bankPropertyReadPlatfomService.retrieveBankPropertName("signature_method");
		BankProperty clientSecretKey = bankPropertyReadPlatfomService
				.retrieveBankPropertName("server_client_secret_key");
		BankProperty serverClientId = bankPropertyReadPlatfomService.retrieveBankPropertName("server_client_id");
		BankProperty miniStatementURL = bankPropertyReadPlatfomService.retrieveBankPropertName("mini_statement_URL");
		BankProperty serverPort = bankPropertyReadPlatfomService.retrieveBankPropertName("server_port");
		BankProperty serverIp = bankPropertyReadPlatfomService.retrieveBankPropertName("server_ip");
		BankProperty walletNumber = bankPropertyReadPlatfomService.retrieveBankPropertName("wallet_number");
		String URL = "https://" + serverIp.getPropertyValue() + ":" + serverPort.getPropertyValue()
				+ miniStatementURL.getPropertyValue() + walletNumber.getPropertyValue();
		String encodedResourceUrl = URLEncoder.encode(URL);
		String method = "GET";
		String signatureCipher = method + "&" + encodedResourceUrl + "&" + timestamp + "&" + nonce + "&"
				+ serverClientId.getPropertyValue() + "&" + clientSecretKey.getPropertyValue();
		MessageDigest messageDigest = MessageDigest.getInstance(signatureMethod.getPropertyValue());
		byte[] signatureBytes = messageDigest.digest(signatureCipher.getBytes());
		String signature = new String(Base64.encodeBase64(signatureBytes));
		String autherisation = new String(Base64.encodeBase64(serverClientId.getPropertyValue().getBytes()));
		HttpGet get = new HttpGet(URL);
		get.setHeader("Timestamp", timeStampString);
		get.setHeader("Nonce", nonce);
		get.setHeader("SignatureMethod", signatureMethod.getPropertyValue());
		get.setHeader("Signature", signature);
		get.setHeader("Content-Type", "application/json");
		get.setHeader("Authorization", "InterswitchAuth " + autherisation);
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		HttpClient httpClient = new DefaultHttpClient();
		LocalDateTime requestDateTime = DateUtils.getLocalDateTimeOfTenant();
		HttpResponse response = httpClient.execute(get);
		HttpEntity respEntity = response.getEntity();
		String responseString = null;
		LocalDateTime responseDateTime = null;
		if (respEntity != null) {
			responseDateTime = DateUtils.getLocalDateTimeOfTenant();
			responseString = EntityUtils.toString(respEntity);
		}
		String insertSql = "INSERT INTO `mpesa_request_response` (`request_date_time`, `request_for`, `request_done_by`, `response_date_time`, `response_body`) VALUES ('"
				+ requestDateTime + "', 'MINISTATEMENT', '" + this.context.authenticatedUser().getId() + "', '"
				+ responseDateTime + "', '" + responseString + "');";
		this.jdbcTemplate.update(insertSql);
		final JsonElement element = this.fromApiJsonHelper.parse(responseString);
		final JsonObject topLevelJsonElement = element.getAsJsonObject();
		final JsonArray array = topLevelJsonElement.get("transactions").getAsJsonArray();
		MpesaMiniStatementTransactions[] mpesaMiniStatementTransactions = new MpesaMiniStatementTransactions[array.size()];
		for (int i = 0; i < array.size(); i++) {
			final JsonObject transaction = array.get(i).getAsJsonObject();
			final String responseCode = this.fromApiJsonHelper.extractStringNamed("responseCode", transaction);
			final String responseMessage = this.fromApiJsonHelper.extractStringNamed("responseMessage", transaction);
			final String transactionType = this.fromApiJsonHelper.extractStringNamed("transactionType", transaction);
			final String walletAccount = this.fromApiJsonHelper.extractStringNamed("walletAccount", transaction);
			final String amount = this.fromApiJsonHelper.extractStringNamed("amount", transaction);
			final String reference = this.fromApiJsonHelper.extractStringNamed("reference", transaction);
			final String provider = this.fromApiJsonHelper.extractStringNamed("provider", transaction);
			final String beneficiaryAccount = this.fromApiJsonHelper.extractStringNamed("beneficiaryAccount",
					transaction);
			final String beneficiaryDetails = this.fromApiJsonHelper.extractStringNamed("beneficiaryDetails",
					transaction);
			final String transactionDate = this.fromApiJsonHelper.extractStringNamed("transactionDate", transaction);
			mpesaMiniStatementTransactions[i] = new MpesaMiniStatementTransactions(responseCode, responseMessage,
					transactionType, walletAccount, amount, reference, provider, beneficiaryAccount, beneficiaryDetails,
					transactionDate);
		}
		return this.toApiJsonSerializer.serialize(settings, mpesaMiniStatementTransactions, PARAMETERS);
	}
	
	@SuppressWarnings("resource")
	@POST
	@Path("/balance")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String chechBalance(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo)
			throws NoSuchAlgorithmException, ClientProtocolException, IOException, JSONException {
		BankProperty timeZone = bankPropertyReadPlatfomService.retrieveBankPropertName("time_zone");
		TimeZone lagosTimeZone = TimeZone.getTimeZone(timeZone.getPropertyValue());
		Calendar calendar = Calendar.getInstance(lagosTimeZone);
		Long timestamp = calendar.getTimeInMillis() / 1000;
		String timeStampString = timestamp.toString();
		UUID uuid = UUID.randomUUID();
		String nonce = uuid.toString().replaceAll("-", "");
		BankProperty signatureMethod = bankPropertyReadPlatfomService.retrieveBankPropertName("signature_method");
		BankProperty clientSecretKey = bankPropertyReadPlatfomService
				.retrieveBankPropertName("server_client_secret_key");
		BankProperty serverClientId = bankPropertyReadPlatfomService.retrieveBankPropertName("server_client_id");
		BankProperty checkBalanceURL = bankPropertyReadPlatfomService.retrieveBankPropertName("balance_URL");
		BankProperty serverPort = bankPropertyReadPlatfomService.retrieveBankPropertName("server_port");
		BankProperty serverIp = bankPropertyReadPlatfomService.retrieveBankPropertName("server_ip");
		BankProperty walletNumber = bankPropertyReadPlatfomService.retrieveBankPropertName("wallet_number");
		String URL = "https://" + serverIp.getPropertyValue() + ":" + serverPort.getPropertyValue()
				+ checkBalanceURL.getPropertyValue() + walletNumber.getPropertyValue();
		String encodedResourceUrl = URLEncoder.encode(URL);
		String method = "GET";
		String signatureCipher = method + "&" + encodedResourceUrl + "&" + timestamp + "&" + nonce + "&"
				+ serverClientId.getPropertyValue() + "&" + clientSecretKey.getPropertyValue();
		MessageDigest messageDigest = MessageDigest.getInstance(signatureMethod.getPropertyValue());
		byte[] signatureBytes = messageDigest.digest(signatureCipher.getBytes());
		String signature = new String(Base64.encodeBase64(signatureBytes));
		String autherisation = new String(Base64.encodeBase64(serverClientId.getPropertyValue().getBytes()));
		HttpGet get = new HttpGet(URL);
		get.setHeader("Timestamp", timeStampString);
		get.setHeader("Nonce", nonce);
		get.setHeader("SignatureMethod", signatureMethod.getPropertyValue());
		get.setHeader("Signature", signature);
		get.setHeader("Content-Type", "application/json");
		get.setHeader("Authorization", "InterswitchAuth " + autherisation);
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		HttpClient httpClient = new DefaultHttpClient();
		LocalDateTime requestDateTime = DateUtils.getLocalDateTimeOfTenant();
		HttpResponse response = httpClient.execute(get);
		HttpEntity respEntity = response.getEntity();
		String responseString = null;
		LocalDateTime responseDateTime = null;
		if (respEntity != null) {
			responseDateTime = DateUtils.getLocalDateTimeOfTenant();
			responseString = EntityUtils.toString(respEntity);
		}
		String insertSql = "INSERT INTO `mpesa_request_response` (`request_date_time`, `request_for`, `request_done_by`, `response_date_time`, `response_body`) VALUES ('"
				+ requestDateTime + "', 'BALANCE', '" + this.context.authenticatedUser().getId() + "', '"
				+ responseDateTime + "', '" + responseString + "');";
		this.jdbcTemplate.update(insertSql);
		final JsonElement element = this.fromApiJsonHelper.parse(responseString);
		final JsonObject topLevelJsonElement = element.getAsJsonObject();
		final String responseCode = this.fromApiJsonHelper.extractStringNamed("responseCode", topLevelJsonElement);
		final String responseMessage = this.fromApiJsonHelper.extractStringNamed("responseMessage", topLevelJsonElement);
		final String availableBalance = this.fromApiJsonHelper.extractStringNamed("availableBalance", topLevelJsonElement);
		final String ledgerBalance = this.fromApiJsonHelper.extractStringNamed("ledgerBalance", topLevelJsonElement);
		MpesaBalanceData mpesaBalanceData = new MpesaBalanceData(responseCode, responseMessage, availableBalance, ledgerBalance);
		return this.toApiJsonSerializer.serialize(settings, mpesaBalanceData, PARAMETERS);
	}
	
	@SuppressWarnings("resource")
	@POST
	@Path("/transaction")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String doTransaction(final String apiRequestBodyAsJson, @Context final UriInfo uriInfo)
			throws NoSuchAlgorithmException, ClientProtocolException, IOException, JSONException {
		final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
		final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
		final String beneficiaryAccount = query.stringValueOfParameterNamed("beneficiaryAccount");
		final String amount = query.stringValueOfParameterNamed("amount");
		final String providerID = "9"+query.stringValueOfParameterNamed("providerID");
		final String narration = query.stringValueOfParameterNamed("narration");
		final String senderName = query.stringValueOfParameterNamed("senderName");
		final String currency = bankPropertyReadPlatfomService.retrieveBankPropertName("currency_code").getPropertyValue();
		final String transactionType = bankPropertyReadPlatfomService.retrieveBankPropertName("transaction_type").getPropertyValue();
		BankProperty timeZone = bankPropertyReadPlatfomService.retrieveBankPropertName("time_zone");
		final String referenceStart = bankPropertyReadPlatfomService.retrieveBankPropertName("transaction_starting").getPropertyValue();
		TimeZone lagosTimeZone = TimeZone.getTimeZone(timeZone.getPropertyValue());
		Calendar calendar = Calendar.getInstance(lagosTimeZone);
		Long timestamp = calendar.getTimeInMillis() / 1000;
		String timeStampString = timestamp.toString();
		UUID uuid = UUID.randomUUID();
		String nonce = uuid.toString().replaceAll("-", "");
		BankProperty signatureMethod = bankPropertyReadPlatfomService.retrieveBankPropertName("signature_method");
		BankProperty clientSecretKey = bankPropertyReadPlatfomService
				.retrieveBankPropertName("server_client_secret_key");
		BankProperty serverClientId = bankPropertyReadPlatfomService.retrieveBankPropertName("server_client_id");
		BankProperty transactionURL = bankPropertyReadPlatfomService.retrieveBankPropertName("transaction_URL");
		BankProperty serverPort = bankPropertyReadPlatfomService.retrieveBankPropertName("server_port");
		BankProperty serverIp = bankPropertyReadPlatfomService.retrieveBankPropertName("server_ip");
		BankProperty walletNumber = bankPropertyReadPlatfomService.retrieveBankPropertName("wallet_number");
		String processingCode = bankPropertyReadPlatfomService.retrieveBankPropertName("processing_code").getPropertyValue();
		String additionalInfo = referenceStart+nonce + "&" + walletNumber.getPropertyValue() + "&" + amount;
		String URL = "https://" + serverIp.getPropertyValue() + ":" + serverPort.getPropertyValue()
				+ transactionURL.getPropertyValue();
		String encodedResourceUrl = URLEncoder.encode(URL);
		String method = "POST";
		String signatureCipher = method + "&" + encodedResourceUrl + "&" + timestamp + "&" + nonce + "&"
				+ serverClientId.getPropertyValue() + "&" + clientSecretKey.getPropertyValue() + "&" + additionalInfo;
		MessageDigest messageDigest = MessageDigest.getInstance(signatureMethod.getPropertyValue());
		byte[] signatureBytes = messageDigest.digest(signatureCipher.getBytes());
		String signature = new String(Base64.encodeBase64(signatureBytes));
		String autherisation = new String(Base64.encodeBase64(serverClientId.getPropertyValue().getBytes()));
		HttpPost post = new HttpPost(URL);
		String json = "{ \"walletAccount\" : \""+walletNumber.getPropertyValue()+"\", \"reference\" : \""+referenceStart+nonce+"\", \"amount\" : "+amount+", \"provider\" : \"BANK\", \"providerID\" : \""+providerID+"\", \"currency\" : "+currency+", \"narration\" :\""+narration+"\", \"beneficiaryAccount\": \""+beneficiaryAccount+"\", \"senderName\" : \""+senderName+"\" , \"transactionType\" :\""+transactionType+"\",\"processingCode\" : \""+processingCode+"\" }";
	    StringEntity entity = new StringEntity(json);
	    post.setEntity(entity);
		post.setHeader("Timestamp", timeStampString);
		post.setHeader("Nonce", nonce);
		post.setHeader("SignatureMethod", signatureMethod.getPropertyValue());
		post.setHeader("Signature", signature);
		post.setHeader("Content-Type", "application/json");
		post.setHeader("Authorization", "InterswitchAuth " + autherisation);
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());
		HttpClient httpClient = new DefaultHttpClient();
		LocalDateTime requestDateTime = DateUtils.getLocalDateTimeOfTenant();
		HttpResponse response = httpClient.execute(post);
		HttpEntity respEntity = response.getEntity();
		String responseString = null;
		LocalDateTime responseDateTime = null;
		if (respEntity != null) {
			responseDateTime = DateUtils.getLocalDateTimeOfTenant();
			responseString = EntityUtils.toString(respEntity);
		}
		String insertSql = "INSERT INTO `mpesa_request_response` (`request_date_time`, `request_for`, `request_done_by`, `response_date_time`, `response_body`, `transaction_id`, `request_body`, `amount`, `providerID`, `narration`, `beneficiaryAccount`, `senderName`) VALUES ('"
				+ requestDateTime + "', 'Transaction', '" + this.context.authenticatedUser().getId() + "', '"
				+ responseDateTime + "', '" + responseString + "','"+referenceStart+nonce+"', '"+json+"',"+amount+","+providerID+",'"+narration+"','"+beneficiaryAccount+"','"+senderName+"');";
		this.jdbcTemplate.update(insertSql);
		final JsonElement element = this.fromApiJsonHelper.parse(responseString);
		final JsonObject topLevelJsonElement = element.getAsJsonObject();
		final String responseCode = this.fromApiJsonHelper.extractStringNamed("responseCode", topLevelJsonElement);
		final String responseMessage = this.fromApiJsonHelper.extractStringNamed("responseMessage", topLevelJsonElement);
		final String rrn = this.fromApiJsonHelper.extractStringNamed("rrn", topLevelJsonElement);
		final String fee = this.fromApiJsonHelper.extractStringNamed("fee", topLevelJsonElement);
		final String reference = this.fromApiJsonHelper.extractStringNamed("reference", topLevelJsonElement);
		final String beneficiaryDetails = this.fromApiJsonHelper.extractStringNamed("beneficiaryDetails", topLevelJsonElement);
		MpesaTransactionData mpesaTransactionData = new MpesaTransactionData(responseCode, responseMessage, rrn, fee, reference, beneficiaryDetails);
		return this.toApiJsonSerializer.serialize(settings, mpesaTransactionData, PARAMETERS);
	}

}
