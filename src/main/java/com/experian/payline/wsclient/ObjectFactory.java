
package com.experian.payline.wsclient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.experian.payline.wsclient package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _PointOfSellComments_QNAME = new QName("http://obj.ws.payline.experian.com", "comments");
    private final static QName _PointOfSellSaleCondURL_QNAME = new QName("http://obj.ws.payline.experian.com", "saleCondURL");
    private final static QName _PointOfSellPrivateLifeURL_QNAME = new QName("http://obj.ws.payline.experian.com", "privateLifeURL");
    private final static QName _PointOfSellEndOfPaymentRedirection_QNAME = new QName("http://obj.ws.payline.experian.com", "endOfPaymentRedirection");
    private final static QName _PointOfSellBuyerMustAcceptSaleCond_QNAME = new QName("http://obj.ws.payline.experian.com", "buyerMustAcceptSaleCond");
    private final static QName _ContractLabel_QNAME = new QName("http://obj.ws.payline.experian.com", "label");
    private final static QName _TransactionThreeDSecure_QNAME = new QName("http://obj.ws.payline.experian.com", "threeDSecure");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.experian.payline.wsclient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetBalanceResponse }
     * 
     */
    public GetBalanceResponse createGetBalanceResponse() {
        return new GetBalanceResponse();
    }

    /**
     * Create an instance of {@link CreateMerchantRequest }
     * 
     */
    public CreateMerchantRequest createCreateMerchantRequest() {
        return new CreateMerchantRequest();
    }

    /**
     * Create an instance of {@link GetMerchantSettingsResponse }
     * 
     */
    public GetMerchantSettingsResponse createGetMerchantSettingsResponse() {
        return new GetMerchantSettingsResponse();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.PointOfSell }
     * 
     */
    public PointOfSell createPointOfSell() {
        return new PointOfSell();
    }

    /**
     * Create an instance of {@link VirtualTerminalFunction }
     * 
     */
    public VirtualTerminalFunction createVirtualTerminalFunction() {
        return new VirtualTerminalFunction();
    }

    /**
     * Create an instance of {@link VirtualTerminal }
     * 
     */
    public VirtualTerminal createVirtualTerminal() {
        return new VirtualTerminal();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.PrivateData }
     * 
     */
    public PrivateData createPrivateData() {
        return new PrivateData();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Transaction }
     * 
     */
    public Transaction createTransaction() {
        return new Transaction();
    }

    /**
     * Create an instance of {@link AssociatedTransactionsList }
     * 
     */
    public AssociatedTransactionsList createAssociatedTransactionsList() {
        return new AssociatedTransactionsList();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Authorization }
     * 
     */
    public Authorization createAuthorization() {
        return new Authorization();
    }

    /**
     * Create an instance of {@link RefundAuthorizationList }
     * 
     */
    public RefundAuthorizationList createRefundAuthorizationList() {
        return new RefundAuthorizationList();
    }

    /**
     * Create an instance of {@link ResetAuthorizationList }
     * 
     */
    public ResetAuthorizationList createResetAuthorizationList() {
        return new ResetAuthorizationList();
    }

    /**
     * Create an instance of {@link ContractNumberWalletList }
     * 
     */
    public ContractNumberWalletList createContractNumberWalletList() {
        return new ContractNumberWalletList();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Wallet }
     * 
     */
    public Wallet createWallet() {
        return new Wallet();
    }

    /**
     * Create an instance of {@link CaptureAuthorizationList }
     * 
     */
    public CaptureAuthorizationList createCaptureAuthorizationList() {
        return new CaptureAuthorizationList();
    }

    /**
     * Create an instance of {@link BankAccountData }
     * 
     */
    public BankAccountData createBankAccountData() {
        return new BankAccountData();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Interlocutor }
     * 
     */
    public Interlocutor createInterlocutor() {
        return new Interlocutor();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Contribution }
     * 
     */
    public Contribution createContribution() {
        return new Contribution();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Capture }
     * 
     */
    public Capture createCapture() {
        return new Capture();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Buyer }
     * 
     */
    public Buyer createBuyer() {
        return new Buyer();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Card }
     * 
     */
    public Card createCard() {
        return new Card();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Recurring }
     * 
     */
    public Recurring createRecurring() {
        return new Recurring();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Key }
     * 
     */
    public Key createKey() {
        return new Key();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Cards }
     * 
     */
    public Cards createCards() {
        return new Cards();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Option }
     * 
     */
    public Option createOption() {
        return new Option();
    }

    /**
     * Create an instance of {@link SelectedContractList }
     * 
     */
    public SelectedContractList createSelectedContractList() {
        return new SelectedContractList();
    }

    /**
     * Create an instance of {@link PrivateDataList }
     * 
     */
    public PrivateDataList createPrivateDataList() {
        return new PrivateDataList();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Contract }
     * 
     */
    public Contract createContract() {
        return new Contract();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.CardOut }
     * 
     */
    public CardOut createCardOut() {
        return new CardOut();
    }

    /**
     * Create an instance of {@link StatusHistory }
     * 
     */
    public StatusHistory createStatusHistory() {
        return new StatusHistory();
    }

    /**
     * Create an instance of {@link ScoringCheque }
     * 
     */
    public ScoringCheque createScoringCheque() {
        return new ScoringCheque();
    }

    /**
     * Create an instance of {@link PaymentAdditional }
     * 
     */
    public PaymentAdditional createPaymentAdditional() {
        return new PaymentAdditional();
    }

    /**
     * Create an instance of {@link AddressInterlocutor }
     * 
     */
    public AddressInterlocutor createAddressInterlocutor() {
        return new AddressInterlocutor();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Owner }
     * 
     */
    public Owner createOwner() {
        return new Owner();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.TicketSend }
     * 
     */
    public TicketSend createTicketSend() {
        return new TicketSend();
    }

    /**
     * Create an instance of {@link TransactionList }
     * 
     */
    public TransactionList createTransactionList() {
        return new TransactionList();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Cheque }
     * 
     */
    public Cheque createCheque() {
        return new Cheque();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Iban }
     * 
     */
    public Iban createIban() {
        return new Iban();
    }

    /**
     * Create an instance of {@link AssociatedTransactions }
     * 
     */
    public AssociatedTransactions createAssociatedTransactions() {
        return new AssociatedTransactions();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Result }
     * 
     */
    public Result createResult() {
        return new Result();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.AddressOwner }
     * 
     */
    public AddressOwner createAddressOwner() {
        return new AddressOwner();
    }

    /**
     * Create an instance of {@link ExtendedCardType }
     * 
     */
    public ExtendedCardType createExtendedCardType() {
        return new ExtendedCardType();
    }

    /**
     * Create an instance of {@link ConnectionData }
     * 
     */
    public ConnectionData createConnectionData() {
        return new ConnectionData();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Order }
     * 
     */
    public Order createOrder() {
        return new Order();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Details }
     * 
     */
    public Details createDetails() {
        return new Details();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Subscription }
     * 
     */
    public Subscription createSubscription() {
        return new Subscription();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Refund }
     * 
     */
    public Refund createRefund() {
        return new Refund();
    }

    /**
     * Create an instance of {@link StatusHistoryList }
     * 
     */
    public StatusHistoryList createStatusHistoryList() {
        return new StatusHistoryList();
    }

    /**
     * Create an instance of {@link CustomPaymentPageCode }
     * 
     */
    public CustomPaymentPageCode createCustomPaymentPageCode() {
        return new CustomPaymentPageCode();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Payment }
     * 
     */
    public Payment createPayment() {
        return new Payment();
    }

    /**
     * Create an instance of {@link PaymentAdditionalList }
     * 
     */
    public PaymentAdditionalList createPaymentAdditionalList() {
        return new PaymentAdditionalList();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.CardsList }
     * 
     */
    public CardsList createCardsList() {
        return new CardsList();
    }

    /**
     * Create an instance of {@link FailedListObject }
     * 
     */
    public FailedListObject createFailedListObject() {
        return new FailedListObject();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.WalletIdList }
     * 
     */
    public WalletIdList createWalletIdList() {
        return new WalletIdList();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Rib }
     * 
     */
    public Rib createRib() {
        return new Rib();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.OrderDetail }
     * 
     */
    public OrderDetail createOrderDetail() {
        return new OrderDetail();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.FailedObject }
     * 
     */
    public FailedObject createFailedObject() {
        return new FailedObject();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.BillingRecord }
     * 
     */
    public BillingRecord createBillingRecord() {
        return new BillingRecord();
    }

    /**
     * Create an instance of {@link BillingRecordList }
     * 
     */
    public BillingRecordList createBillingRecordList() {
        return new BillingRecordList();
    }

    /**
     * Create an instance of {@link TechnicalData }
     * 
     */
    public TechnicalData createTechnicalData() {
        return new TechnicalData();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.Address }
     * 
     */
    public Address createAddress() {
        return new Address();
    }

    /**
     * Create an instance of {@link Authentication3DSecure }
     * 
     */
    public Authentication3DSecure createAuthentication3DSecure() {
        return new Authentication3DSecure();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.BankAccount }
     * 
     */
    public BankAccount createBankAccount() {
        return new BankAccount();
    }

    /**
     * Create an instance of {@link GetTransactionDetailsResponse }
     * 
     */
    public GetTransactionDetailsResponse createGetTransactionDetailsResponse() {
        return new GetTransactionDetailsResponse();
    }

    /**
     * Create an instance of {@link TransactionsSearchResponse }
     * 
     */
    public TransactionsSearchResponse createTransactionsSearchResponse() {
        return new TransactionsSearchResponse();
    }

    /**
     * Create an instance of {@link GetWebWalletResponse }
     * 
     */
    public GetWebWalletResponse createGetWebWalletResponse() {
        return new GetWebWalletResponse();
    }

    /**
     * Create an instance of {@link UpdateWebWalletResponse }
     * 
     */
    public UpdateWebWalletResponse createUpdateWebWalletResponse() {
        return new UpdateWebWalletResponse();
    }

    /**
     * Create an instance of {@link DoScheduledWalletPaymentRequest }
     * 
     */
    public DoScheduledWalletPaymentRequest createDoScheduledWalletPaymentRequest() {
        return new DoScheduledWalletPaymentRequest();
    }

    /**
     * Create an instance of {@link GetCardsResponse }
     * 
     */
    public GetCardsResponse createGetCardsResponse() {
        return new GetCardsResponse();
    }

    /**
     * Create an instance of {@link DoReAuthorizationRequest }
     * 
     */
    public DoReAuthorizationRequest createDoReAuthorizationRequest() {
        return new DoReAuthorizationRequest();
    }

    /**
     * Create an instance of {@link DoRecurrentWalletPaymentResponse }
     * 
     */
    public DoRecurrentWalletPaymentResponse createDoRecurrentWalletPaymentResponse() {
        return new DoRecurrentWalletPaymentResponse();
    }

    /**
     * Create an instance of {@link DoResetRequest }
     * 
     */
    public DoResetRequest createDoResetRequest() {
        return new DoResetRequest();
    }

    /**
     * Create an instance of {@link EnableWalletResponse }
     * 
     */
    public EnableWalletResponse createEnableWalletResponse() {
        return new EnableWalletResponse();
    }

    /**
     * Create an instance of {@link DoImmediateWalletPaymentRequest }
     * 
     */
    public DoImmediateWalletPaymentRequest createDoImmediateWalletPaymentRequest() {
        return new DoImmediateWalletPaymentRequest();
    }

    /**
     * Create an instance of {@link DoRecurrentWalletPaymentRequest }
     * 
     */
    public DoRecurrentWalletPaymentRequest createDoRecurrentWalletPaymentRequest() {
        return new DoRecurrentWalletPaymentRequest();
    }

    /**
     * Create an instance of {@link GetMassTraitmentDetailsRequest }
     * 
     */
    public GetMassTraitmentDetailsRequest createGetMassTraitmentDetailsRequest() {
        return new GetMassTraitmentDetailsRequest();
    }

    /**
     * Create an instance of {@link DoMassRefundResponse }
     * 
     */
    public DoMassRefundResponse createDoMassRefundResponse() {
        return new DoMassRefundResponse();
    }

    /**
     * Create an instance of {@link VerifyAuthenticationResponse }
     * 
     */
    public VerifyAuthenticationResponse createVerifyAuthenticationResponse() {
        return new VerifyAuthenticationResponse();
    }

    /**
     * Create an instance of {@link DisablePaymentRecordRequest }
     * 
     */
    public DisablePaymentRecordRequest createDisablePaymentRecordRequest() {
        return new DisablePaymentRecordRequest();
    }

    /**
     * Create an instance of {@link DoAuthorizationRequest }
     * 
     */
    public DoAuthorizationRequest createDoAuthorizationRequest() {
        return new DoAuthorizationRequest();
    }

    /**
     * Create an instance of {@link DoCaptureResponse }
     * 
     */
    public DoCaptureResponse createDoCaptureResponse() {
        return new DoCaptureResponse();
    }

    /**
     * Create an instance of {@link DoRefundRequest }
     * 
     */
    public DoRefundRequest createDoRefundRequest() {
        return new DoRefundRequest();
    }

    /**
     * Create an instance of {@link UpdateWalletRequest }
     * 
     */
    public UpdateWalletRequest createUpdateWalletRequest() {
        return new UpdateWalletRequest();
    }

    /**
     * Create an instance of {@link DisablePaymentRecordResponse }
     * 
     */
    public DisablePaymentRecordResponse createDisablePaymentRecordResponse() {
        return new DisablePaymentRecordResponse();
    }

    /**
     * Create an instance of {@link EnableWalletRequest }
     * 
     */
    public EnableWalletRequest createEnableWalletRequest() {
        return new EnableWalletRequest();
    }

    /**
     * Create an instance of {@link DoImmediateWalletPaymentResponse }
     * 
     */
    public DoImmediateWalletPaymentResponse createDoImmediateWalletPaymentResponse() {
        return new DoImmediateWalletPaymentResponse();
    }

    /**
     * Create an instance of {@link DoRefundResponse }
     * 
     */
    public DoRefundResponse createDoRefundResponse() {
        return new DoRefundResponse();
    }

    /**
     * Create an instance of {@link GetBalanceResponse.Balance }
     * 
     */
    public GetBalanceResponse.Balance createGetBalanceResponseBalance() {
        return new GetBalanceResponse.Balance();
    }

    /**
     * Create an instance of {@link CreateMerchantRequest.NationalID }
     * 
     */
    public CreateMerchantRequest.NationalID createCreateMerchantRequestNationalID() {
        return new CreateMerchantRequest.NationalID();
    }

    /**
     * Create an instance of {@link CreateMerchantRequest.Poss }
     * 
     */
    public CreateMerchantRequest.Poss createCreateMerchantRequestPoss() {
        return new CreateMerchantRequest.Poss();
    }

    /**
     * Create an instance of {@link DoMassResetResponse }
     * 
     */
    public DoMassResetResponse createDoMassResetResponse() {
        return new DoMassResetResponse();
    }

    /**
     * Create an instance of {@link GetWalletResponse }
     * 
     */
    public GetWalletResponse createGetWalletResponse() {
        return new GetWalletResponse();
    }

    /**
     * Create an instance of {@link DoScheduledWalletPaymentResponse }
     * 
     */
    public DoScheduledWalletPaymentResponse createDoScheduledWalletPaymentResponse() {
        return new DoScheduledWalletPaymentResponse();
    }

    /**
     * Create an instance of {@link UpdateWalletResponse }
     * 
     */
    public UpdateWalletResponse createUpdateWalletResponse() {
        return new UpdateWalletResponse();
    }

    /**
     * Create an instance of {@link DoWebPaymentRequest }
     * 
     */
    public DoWebPaymentRequest createDoWebPaymentRequest() {
        return new DoWebPaymentRequest();
    }

    /**
     * Create an instance of {@link TransactionsSearchRequest }
     * 
     */
    public TransactionsSearchRequest createTransactionsSearchRequest() {
        return new TransactionsSearchRequest();
    }

    /**
     * Create an instance of {@link DoMassCaptureResponse }
     * 
     */
    public DoMassCaptureResponse createDoMassCaptureResponse() {
        return new DoMassCaptureResponse();
    }

    /**
     * Create an instance of {@link DoCaptureRequest }
     * 
     */
    public DoCaptureRequest createDoCaptureRequest() {
        return new DoCaptureRequest();
    }

    /**
     * Create an instance of {@link GetEncryptionKeyResponse }
     * 
     */
    public GetEncryptionKeyResponse createGetEncryptionKeyResponse() {
        return new GetEncryptionKeyResponse();
    }

    /**
     * Create an instance of {@link DoAuthorizationResponse }
     * 
     */
    public DoAuthorizationResponse createDoAuthorizationResponse() {
        return new DoAuthorizationResponse();
    }

    /**
     * Create an instance of {@link DisableWalletRequest }
     * 
     */
    public DisableWalletRequest createDisableWalletRequest() {
        return new DisableWalletRequest();
    }

    /**
     * Create an instance of {@link GetEncryptionKeyRequest }
     * 
     */
    public GetEncryptionKeyRequest createGetEncryptionKeyRequest() {
        return new GetEncryptionKeyRequest();
    }

    /**
     * Create an instance of {@link GetWebPaymentDetailsRequest }
     * 
     */
    public GetWebPaymentDetailsRequest createGetWebPaymentDetailsRequest() {
        return new GetWebPaymentDetailsRequest();
    }

    /**
     * Create an instance of {@link GetWebWalletRequest }
     * 
     */
    public GetWebWalletRequest createGetWebWalletRequest() {
        return new GetWebWalletRequest();
    }

    /**
     * Create an instance of {@link DoMassResetRequest }
     * 
     */
    public DoMassResetRequest createDoMassResetRequest() {
        return new DoMassResetRequest();
    }

    /**
     * Create an instance of {@link DoReAuthorizationResponse }
     * 
     */
    public DoReAuthorizationResponse createDoReAuthorizationResponse() {
        return new DoReAuthorizationResponse();
    }

    /**
     * Create an instance of {@link GetMerchantSettingsRequest }
     * 
     */
    public GetMerchantSettingsRequest createGetMerchantSettingsRequest() {
        return new GetMerchantSettingsRequest();
    }

    /**
     * Create an instance of {@link CreateWalletResponse }
     * 
     */
    public CreateWalletResponse createCreateWalletResponse() {
        return new CreateWalletResponse();
    }

    /**
     * Create an instance of {@link DoScoringChequeRequest }
     * 
     */
    public DoScoringChequeRequest createDoScoringChequeRequest() {
        return new DoScoringChequeRequest();
    }

    /**
     * Create an instance of {@link CreateWebWalletRequest }
     * 
     */
    public CreateWebWalletRequest createCreateWebWalletRequest() {
        return new CreateWebWalletRequest();
    }

    /**
     * Create an instance of {@link DoMassCaptureRequest }
     * 
     */
    public DoMassCaptureRequest createDoMassCaptureRequest() {
        return new DoMassCaptureRequest();
    }

    /**
     * Create an instance of {@link DoResetResponse }
     * 
     */
    public DoResetResponse createDoResetResponse() {
        return new DoResetResponse();
    }

    /**
     * Create an instance of {@link GetMassTraitmentDetailsResponse }
     * 
     */
    public GetMassTraitmentDetailsResponse createGetMassTraitmentDetailsResponse() {
        return new GetMassTraitmentDetailsResponse();
    }

    /**
     * Create an instance of {@link DoCreditRequest }
     * 
     */
    public DoCreditRequest createDoCreditRequest() {
        return new DoCreditRequest();
    }

    /**
     * Create an instance of {@link GetCardsRequest }
     * 
     */
    public GetCardsRequest createGetCardsRequest() {
        return new GetCardsRequest();
    }

    /**
     * Create an instance of {@link VerifyEnrollmentRequest }
     * 
     */
    public VerifyEnrollmentRequest createVerifyEnrollmentRequest() {
        return new VerifyEnrollmentRequest();
    }

    /**
     * Create an instance of {@link VerifyAuthenticationRequest }
     * 
     */
    public VerifyAuthenticationRequest createVerifyAuthenticationRequest() {
        return new VerifyAuthenticationRequest();
    }

    /**
     * Create an instance of {@link VerifyEnrollmentResponse }
     * 
     */
    public VerifyEnrollmentResponse createVerifyEnrollmentResponse() {
        return new VerifyEnrollmentResponse();
    }

    /**
     * Create an instance of {@link CreateWebWalletResponse }
     * 
     */
    public CreateWebWalletResponse createCreateWebWalletResponse() {
        return new CreateWebWalletResponse();
    }

    /**
     * Create an instance of {@link DisableWalletResponse }
     * 
     */
    public DisableWalletResponse createDisableWalletResponse() {
        return new DisableWalletResponse();
    }

    /**
     * Create an instance of {@link DoCreditResponse }
     * 
     */
    public DoCreditResponse createDoCreditResponse() {
        return new DoCreditResponse();
    }

    /**
     * Create an instance of {@link DoScoringChequeResponse }
     * 
     */
    public DoScoringChequeResponse createDoScoringChequeResponse() {
        return new DoScoringChequeResponse();
    }

    /**
     * Create an instance of {@link GetWalletRequest }
     * 
     */
    public GetWalletRequest createGetWalletRequest() {
        return new GetWalletRequest();
    }

    /**
     * Create an instance of {@link GetMerchantSettingsResponse.ListPointOfSell }
     * 
     */
    public GetMerchantSettingsResponse.ListPointOfSell createGetMerchantSettingsResponseListPointOfSell() {
        return new GetMerchantSettingsResponse.ListPointOfSell();
    }

    /**
     * Create an instance of {@link GetPaymentRecordResponse }
     * 
     */
    public GetPaymentRecordResponse createGetPaymentRecordResponse() {
        return new GetPaymentRecordResponse();
    }

    /**
     * Create an instance of {@link DoDebitRequest }
     * 
     */
    public DoDebitRequest createDoDebitRequest() {
        return new DoDebitRequest();
    }

    /**
     * Create an instance of {@link DoWebPaymentResponse }
     * 
     */
    public DoWebPaymentResponse createDoWebPaymentResponse() {
        return new DoWebPaymentResponse();
    }

    /**
     * Create an instance of {@link UpdateWebWalletRequest }
     * 
     */
    public UpdateWebWalletRequest createUpdateWebWalletRequest() {
        return new UpdateWebWalletRequest();
    }

    /**
     * Create an instance of {@link DoMassRefundRequest }
     * 
     */
    public DoMassRefundRequest createDoMassRefundRequest() {
        return new DoMassRefundRequest();
    }

    /**
     * Create an instance of {@link GetBalanceRequest }
     * 
     */
    public GetBalanceRequest createGetBalanceRequest() {
        return new GetBalanceRequest();
    }

    /**
     * Create an instance of {@link GetPaymentRecordRequest }
     * 
     */
    public GetPaymentRecordRequest createGetPaymentRecordRequest() {
        return new GetPaymentRecordRequest();
    }

    /**
     * Create an instance of {@link GetWebPaymentDetailsResponse }
     * 
     */
    public GetWebPaymentDetailsResponse createGetWebPaymentDetailsResponse() {
        return new GetWebPaymentDetailsResponse();
    }

    /**
     * Create an instance of {@link CreateWalletRequest }
     * 
     */
    public CreateWalletRequest createCreateWalletRequest() {
        return new CreateWalletRequest();
    }

    /**
     * Create an instance of {@link CreateMerchantResponse }
     * 
     */
    public CreateMerchantResponse createCreateMerchantResponse() {
        return new CreateMerchantResponse();
    }

    /**
     * Create an instance of {@link DoDebitResponse }
     * 
     */
    public DoDebitResponse createDoDebitResponse() {
        return new DoDebitResponse();
    }

    /**
     * Create an instance of {@link GetTransactionDetailsRequest }
     * 
     */
    public GetTransactionDetailsRequest createGetTransactionDetailsRequest() {
        return new GetTransactionDetailsRequest();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.PointOfSell.Contracts }
     * 
     */
    public PointOfSell.Contracts createPointOfSellContracts() {
        return new PointOfSell.Contracts();
    }

    /**
     * Create an instance of {@link com.experian.payline.wsclient.PointOfSell.CustomPaymentPageCodeList }
     * 
     */
    public PointOfSell.CustomPaymentPageCodeList createPointOfSellCustomPaymentPageCodeList() {
        return new PointOfSell.CustomPaymentPageCodeList();
    }

    /**
     * Create an instance of {@link VirtualTerminalFunction.FunctionParameter }
     * 
     */
    public VirtualTerminalFunction.FunctionParameter createVirtualTerminalFunctionFunctionParameter() {
        return new VirtualTerminalFunction.FunctionParameter();
    }

    /**
     * Create an instance of {@link VirtualTerminal.Functions }
     * 
     */
    public VirtualTerminal.Functions createVirtualTerminalFunctions() {
        return new VirtualTerminal.Functions();
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://obj.ws.payline.experian.com", name = "comments", scope = PointOfSell.class)
    public JAXBElement<String> createPointOfSellComments(String value) {
        return new JAXBElement<String>(_PointOfSellComments_QNAME, String.class, PointOfSell.class, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://obj.ws.payline.experian.com", name = "saleCondURL", scope = PointOfSell.class)
    public JAXBElement<String> createPointOfSellSaleCondURL(String value) {
        return new JAXBElement<String>(_PointOfSellSaleCondURL_QNAME, String.class, PointOfSell.class, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://obj.ws.payline.experian.com", name = "privateLifeURL", scope = PointOfSell.class)
    public JAXBElement<String> createPointOfSellPrivateLifeURL(String value) {
        return new JAXBElement<String>(_PointOfSellPrivateLifeURL_QNAME, String.class, PointOfSell.class, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://obj.ws.payline.experian.com", name = "endOfPaymentRedirection", scope = PointOfSell.class)
    public JAXBElement<Boolean> createPointOfSellEndOfPaymentRedirection(Boolean value) {
        return new JAXBElement<Boolean>(_PointOfSellEndOfPaymentRedirection_QNAME, Boolean.class, PointOfSell.class, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://obj.ws.payline.experian.com", name = "buyerMustAcceptSaleCond", scope = PointOfSell.class)
    public JAXBElement<Boolean> createPointOfSellBuyerMustAcceptSaleCond(Boolean value) {
        return new JAXBElement<Boolean>(_PointOfSellBuyerMustAcceptSaleCond_QNAME, Boolean.class, PointOfSell.class, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://obj.ws.payline.experian.com", name = "label", scope = Contract.class)
    public JAXBElement<String> createContractLabel(String value) {
        return new JAXBElement<String>(_ContractLabel_QNAME, String.class, Contract.class, value);
    }

    /**
     * Create an instance of {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://obj.ws.payline.experian.com", name = "threeDSecure", scope = Transaction.class)
    public JAXBElement<String> createTransactionThreeDSecure(String value) {
        return new JAXBElement<String>(_TransactionThreeDSecure_QNAME, String.class, Transaction.class, value);
    }

}
