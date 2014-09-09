using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using FarInfo.DataAccess.Dao.Interface;
using FarInfo.Services.services.Share;
using FarInfo.Model.Model;
using FarInfo.Contrats.Contrats;
using System.ServiceModel.Activation;
using FarInfo.DataAccess.Dao.Share;
using FarInfo.ExceptionHandler.ExceptionHandling;
using FarInfo.Contrats.Models.vo;
using FarInfo.Contrats.Interface;
using System.Configuration;
using System.Web;
using System.IO;
using System.Net;
using System.Collections.Specialized;
using FarInfo.Contrats.Models.PAYBOX_DataSet;
using FarInfo.DataAccess.Dao.Share.Helpers_Dao_Shared.Enum;
using System.Data;

namespace FarInfo.Services.Services
{
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class PayBoxClientDirectPlusService : IPayBoxClientDirectPlusService
    {
        #region Properties

        string _uriPayBoxDirect1 = ConfigurationManager.AppSettings["uriPayBoxDirect1"],
            _uriPayBoxDirect2 = ConfigurationManager.AppSettings["uriPayBoxDirect2"],
            _uriPayBoxDirect3 = ConfigurationManager.AppSettings["uriPayBoxDirect3"];


        #endregion

        #region DAO

        /// <summary>
        /// 
        /// </summary>
        public IComptabilitesDao ComptabilitesDAO { get; set; }

        /// <summary>
        /// 
        /// </summary>
        public IEcheanceDao EcheanceDAO { get; set; }

        /// <summary>
        /// 
        /// </summary>
        public IAffecterDao AffecterDAO { get; set; }

        /// <summary>
        /// 
        /// </summary>
        public IClientDao ClientDAO { get; set; }
        /// <summary>
        /// 
        /// </summary>
        public IServiceContext Context { get; set; }
        /// <summary>
        /// 
        /// </summary>
        public IPayBoxHistoriqueDao PayBoxHistoriqueDAO { get; set; }
        /// <summary>
        /// 
        /// </summary>
        public IReglementDao ReglementDAO { get; set; }
        #endregion

        #region IPayBoxClientDirectPlusService Members

        /// <summary>
        /// Ajouter un nouveau client Paybox
        /// </summary>
        /// <param name="idClient"></param>
        /// <param name="codeProduit"></param>
        /// <param name="idAgence"></param>
        /// <param name="idUser"></param>
        /// <returns></returns>
        public string AjouterClientPaybox(long idClient, string codeProduit, long idAgence, long idUser)
        {
            string s = "";
            try
            {
                var client = ClientDAO.GetEntityById(idClient);

                if (client != null)
                {
                    var banque = GetBanqueByCodeProduitTypeCB(codeProduit, client.CB_TYPEID, idAgence);

                    if (client.PAYBOXCLIENT_PRODUIT != null && client.PAYBOXCLIENT_PRODUIT.Count(c => c.CODE_PRODUIT == codeProduit
                                                                                                   && c.CODE_BANQUE == banque.CODE_BANQUE
                                                                                                   && c.CB_TYPEID == client.CB_TYPEID
                                                                                                ) > 0)
                    {
                        s = "Le client existe déjà dans paybox";
                    }
                    else // création compatibilite V2
                    {
                        var response = this.AddDeleteConsulterClientCompatibleV2(idClient, banque, codeProduit, ClientDAO.NextCurrentValue().ToString(), "00056", idUser, client.CB_TYPEID);
                        if (response != null && response.Count > 0)
                        {
                            s = this.GetResponseByCode(response[2]);
                            client.CB_PAYBOX_PORTEUR = response[3];
                            ClientDAO.SaveChanges();
                        }
                    }
                }
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return s;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="idClient"></param>
        /// <param name="codeProduit"></param>
        /// <param name="montant"></param>
        /// <param name="idAgence"></param>
        /// <param name="idUser"></param>
        /// <returns></returns>
        public string CrediterClientPaybox(long idClient, string codeProduit, double montant, long idAgence, long idUser)
        {
            string s = "";
            try
            {
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return s;
        }

        /// <summary>
        /// Modifier un client Paybox 
        /// </summary>
        /// <param name="idClient"></param>
        /// <param name="codeProduit"></param>
        /// <param name="idAgence"></param>
        /// <param name="idUser"></param>
        /// <returns></returns>
        public string ModifierClientPaybox(long idClient, string codeProduit, long idAgence, long idUser)
        {
            string s = "";
            try
            {
                var client = ClientDAO.GetEntityById(idClient);

                if (client != null)
                {
                    var banque = GetBanqueByCodeProduitTypeCB(codeProduit, client.CB_TYPEID, idAgence);
                    var response = this.AddDeleteConsulterClientCompatibleV2(idClient, banque, codeProduit, ClientDAO.NextCurrentValue().ToString(), "00057", idUser, client.CB_TYPEID);
                    if (response != null && response.Count > 0)
                    {
                        s = this.GetResponseByCode(response[2]);
                        client.CB_PAYBOX_PORTEUR = response[3];
                        ClientDAO.SaveChanges();
                    }
                }
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return s;
        }

        /// <summary>
        /// Supprimer un client Paybox
        /// </summary>
        /// <param name="idClient"></param>
        /// <param name="codeProduit"></param>
        /// <param name="idAgence"></param>
        /// <param name="idUser"></param>
        /// <returns></returns>
        public string SupprimerClientPaybox(long idClient, string codeProduit, long idAgence, long idUser)
        {
            string s = "";
            try
            {
                var client = ClientDAO.GetEntityById(idClient);

                if (client != null)
                {
                    var refCommande = this.ClientDAO.NextCurrentValue();
                    var banque = this.GetBanqueByCodeProduitTypeCB(codeProduit, client.CB_TYPEID, idClient);
                    var response = this.AddDeleteConsulterClientCompatibleV2(idClient, banque, codeProduit, refCommande.ToString(), "00058", idUser, client.CB_TYPEID);

                    if (response != null && response.Count > 0)
                    {
                        s = this.GetCodeResponse(response[2]);

                        if (response[2] == "00000")
                        {
                            var payBoxProduit = client.PAYBOXCLIENT_PRODUIT.SingleOrDefault(ss => ss.CODE_BANQUE == banque.CODE_BANQUE && ss.CODE_PRODUIT == codeProduit && ss.REF_ABONNE == response[4]);
                            if (payBoxProduit != null)
                            {
                                ClientDAO.DeletePayboxProduit(payBoxProduit);
                            }
                        }

                        client.CB_PAYBOX_PORTEUR = null;
                    }
                }
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return s;
        }

        /// <summary>
        /// Consulter une transaction. Pas encore implementé
        /// </summary>
        /// <param name="idClient"></param>
        /// <param name="codeProduit"></param>
        /// <param name="idAgence"></param>
        /// <param name="idUser"></param>
        /// <returns></returns>
        public string ConsulterClientPaybox(long idClient, string codeProduit, long idAgence, long idUser)
        {
            string s = "";
            try
            {
                var client = ClientDAO.GetEntityById(idClient);

                if (client != null)
                {
                    var refCommande = this.ClientDAO.NextCurrentValue();
                    var banque = this.GetBanqueByCodeProduitTypeCB(codeProduit, client.CB_TYPEID, idClient);
                    if (client.PAYBOXCLIENT_PRODUIT != null && client.PAYBOXCLIENT_PRODUIT.Count(ss => ss.CODE_BANQUE == banque.CODE_BANQUE && ss.CODE_PRODUIT == codeProduit) > 0)
                    {
                        var payboxProduit = client.PAYBOXCLIENT_PRODUIT.SingleOrDefault(ss => ss.CODE_BANQUE == banque.CODE_BANQUE && ss.CODE_PRODUIT == codeProduit);
                        var postData = this.CreationPostData(banque, payboxProduit, "00017", 0.01, codeProduit, idClient);

                        if (postData != null)
                        {
                            var response = this.RequestPaybox(postData);

                            #region create payBoxHistorique
                            CreatePayBoxHistorique(idClient, idUser, client.CB_TYPEID, banque.CODE_BANQUE, postData, response, null);
                            #endregion
                        }
                    }
                    else
                    {
                        var response = this.AddDeleteConsulterClientCompatibleV2(idClient, banque, codeProduit, ClientDAO.NextCurrentValue().ToString(), "00017", idUser, client.CB_TYPEID);
                        s = this.GetCodeResponse(response[2]);
                    }
                }
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return s;
        }

        /// <summary>
        /// Debiter un client paybox
        /// </summary>
        /// <param name="idClient"></param>
        /// <param name="codeProduit"></param>
        /// <param name="idAgence"></param>
        /// <returns></returns>
        public PayBoxHistoriqueVo DebiterClientPaybox(long idClient, double montant, string codeProduit, long idAgence, long idUser, long? idEcheance)
        {
            PayBoxHistoriqueVo vo = null;
            List<string> response = null;
            NameValueCollection postData = null;
            PAYBOXCLIENT_PRODUIT payboxProduit = null;
            BANQUE banque = null;
            try
            {
                var client = ClientDAO.GetEntityById(idClient);
                banque = GetBanqueByCodeProduitTypeCB(codeProduit, client.CB_TYPEID, idAgence);
                if (client != null)
                {


                    if (client.PAYBOXCLIENT_PRODUIT != null && client.PAYBOXCLIENT_PRODUIT.Count(c => c.CODE_PRODUIT == codeProduit
                                                                                                   && c.CODE_BANQUE == banque.CODE_BANQUE
                                                                                                   && c.CB_TYPEID == client.CB_TYPEID
                                                                                                ) > 0)
                    {
                        payboxProduit = client.PAYBOXCLIENT_PRODUIT.SingleOrDefault(c => c.CODE_PRODUIT == codeProduit && c.CODE_BANQUE == banque.CODE_BANQUE);
                    }
                    else
                    {
                        #region création du client puis debit
                        response = this.AddDeleteConsulterClientCompatibleV2(idClient, banque, codeProduit, ClientDAO.NextCurrentValue().ToString(), "00056", idUser, client.CB_TYPEID);
                        if (response != null && response.Count > 0 && response[2] == "00000")
                        {
                            //actualisé le client
                            client = ClientDAO.GetEntityById(idClient);
                            payboxProduit = client.PAYBOXCLIENT_PRODUIT.SingleOrDefault(c => c.CODE_PRODUIT == codeProduit && c.CODE_BANQUE == banque.CODE_BANQUE);
                        }
                        #endregion
                    }
                }

                #region client existe

                try
                {
                    postData = this.CreationPostData(banque, payboxProduit, "00003", montant, codeProduit, idClient);
                }
                catch (Exception ex)
                {
                    new Exception("ERREUR DE CREATION DES DONNEES DE DEBIT DE PAYBOX", ex);
                    throw;
                }

                if (postData != null && postData.Count > 0)
                {
                    try
                    {
                        response = this.RequestPaybox(postData);
                    }
                    catch (Exception exc)
                    {
                        exc.HelpLink = "ERROR DE DEBIT PAYBOX";
                        ExceptionManager.Handle(exc, ApplicationLayer.Service);
                        throw;
                    }

                    if (response != null && response.Count > 0)
                    {
                        vo = new PayBoxHistoriqueVo()
                        {
                            cbTypeId = client.CB_TYPEID,
                            codeBanque = banque.CODE_BANQUE,
                            httpStatus = (response[2] != "00001") ? "200" : response[2],
                            pbStatus = response[2],
                            pb_QRYNum = Convert.ToInt64(postData.GetValues("NUMQUESTION")[0]),
                            pbTrameQry = response[0],
                            opdType = postData.GetValues("TYPE")[0],
                            opDate = DateTime.Now,
                            pbTrameResponse = response[1],
                            response = this.GetResponseByCode(response[2]),
                            responses = response
                        };
                    }

                    // Création des élements comptabilite
                    if (response != null && response.Count > 0 && response[2] == "00000")
                    {
                        try
                        {
                            #region Création des elements comptables
                            if (response[2] == "00000")
                            {
                                ECRITURE ecriture = new ECRITURE()
                                {
                                    TYPE = 1,
                                    SENS = "C",
                                    DTECRITURE = DateTime.Now.Date,
                                    MONTANT = Convert.ToDecimal(montant),
                                    ID_CLIENT = idClient,
                                    ID_AGENCE = idAgence,
                                    CODE_PRODUIT = (codeProduit == "DOM") ? "CAD" : codeProduit
                                };

                                long idEcriture = ReglementDAO.CreateEcriture(ecriture);

                                REGLEMENT reg = new REGLEMENT()
                                {
                                    CODE_BANQUE = banque.CODE_BANQUE,
                                    CODE_PRODUIT = (codeProduit == "DOM" || codeProduit == "CAD") ? "CAD" : codeProduit,
                                    DATE_BANQUE = DateTime.Now.Date,
                                    DATE_REMISE = DateTime.Now.Date,
                                    DATE_SAISIE = DateTime.Now.Date,
                                    DATE_VALIDATION = DateTime.Now.Date,
                                    ID_AGENCE = idAgence,
                                    ID_CLIENT = idClient,
                                    ID_MODE_REGLEMENT = 3,
                                    NUM_MANIP = this.ReglementDAO.NextValueNumManip(),
                                    MONTANT = Convert.ToDecimal(montant),
                                    REG_CBTYPE = client.CB_TYPEID,
                                    ROW_VERSION = DateTime.Now,
                                    SOLDE = Convert.ToDecimal(montant),
                                    TYPE = 3,
                                    ID_ECRITURE = idEcriture
                                };

                                long idReglement = ReglementDAO.Create(reg);

                                if (idReglement > 0)
                                    vo.idReglement = idReglement;
                            }
                            #endregion
                        }
                        catch (Exception ex)
                        {
                            ex.HelpLink = "ERROR DE CREATION DES ELEMENTS COMPTABLES. DEBIT EFFECTUE. CONTACT LE SERVICE INFORMATIQUE";
                            ExceptionManager.Handle(ex, ApplicationLayer.Service);
                            this.CreatePayBoxHistorique(idClient, idUser, client.CB_TYPEID, banque.CODE_BANQUE, postData, response, null);
                            throw;
                        }

                        if (response.Count >= 4 && !string.IsNullOrEmpty(response[3]))
                        {
                            client.CB_PAYBOX_PORTEUR = response[3];
                        }

                        try
                        {
                            // envoie le ticket de debit du client
                            if (!string.IsNullOrEmpty(client.EMAIL))
                            {
                                var postData2 = HttpUtility.ParseQueryString(response[0]);
                                var responseData = HttpUtility.ParseQueryString(response[1]);
                                string nom = client.NOM.ToUpper() + (string.IsNullOrEmpty(client.PRENOM) ? " " : " " + client.PRENOM.ToUpper());
                                string civilite = "";
                                var date = idEcheance.HasValue ? this.ReglementDAO.GetDateEcheanceByIdEcheance(idEcheance) : DateTime.Now;
                                if (client.ID_CIVILITE.HasValue)
                                    civilite = this.ClientDAO.GetCivilite(client.ID_CIVILITE.Value);
                                SendMailClientTicketPaiement(postData2, responseData, client.EMAIL, nom, codeProduit, client.CB_TYPEID, montant, date, civilite, idClient);
                            }
                        }
                        catch (Exception ex)
                        {
                            ex.HelpLink = "ERROR D' ENVOI DU TICKET CLIENT!";
                            ExceptionManager.Handle(ex, ApplicationLayer.Service);
                            throw;
                        }
                    }
                }
                #endregion

                client.CB_PAYBOX_PORTEUR = response[3];
                ClientDAO.SaveChanges();
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return vo;
        }

        #endregion

        #region private methode

        /// <summary>
        /// Retourne la banque par rapport à un code produit, type de cb et l'agance
        /// </summary>
        /// <param name="codeProduit">le type de produit</param>
        /// <param name="typeCB">le type de cb</param>
        /// <param name="idAgence">id agence</param>
        /// <returns>banque</returns>
        private BANQUE GetBanqueByCodeProduitTypeCB(string codeProduit, string typeCB, long idAgence)
        {
            BANQUE entity = null;
            try
            {
                if ((typeCB == "CB" || typeCB == "COFI") && codeProduit != "CAD")
                {
                    //BRED
                    entity = ClientDAO.GetBanqueByCodeBanque("BP");
                }
                else if (codeProduit == "CAD")
                {
                    if (idAgence == 1)
                    {
                        //BRED
                        entity = ClientDAO.GetBanqueByCodeBanque("BP");
                    }
                    else
                    {
                        entity = ClientDAO.GetBanqueByCodeBanque("CA");
                    }
                }
                else if (typeCB == "AMEX")
                {
                    entity = ClientDAO.GetBanqueByCodeBanque("SG");
                }
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return entity;
        }

        /// <summary>
        /// Verifie le code retour de paybox
        /// </summary>
        /// <param name="response"></param>
        /// <returns></returns>
        private string GetCodeResponse(string response)
        {
            string s = "";
            try
            {
                string codeResponse = response.Substring(response.IndexOf("CODEREPONSE=") + "CODEREPONSE=".Length, 5);

                switch (codeResponse)
                {
                    case "00000":
                        s = "Opération réussie.";
                        break;
                    case "00001":
                        s = "La connexion au centre d'autorisation a échouer sur les 3 urls de PayBox." +
                            "\n\r Veuillez contacter le service informatique";
                        break;
                    case "001xx":
                        s = "Paiement refusé par le centre d'autorisation." +
                            "\n\rEn cas d'autorisation de la transaction par le centre d?autorisation de la banque, le résultat 00100 sera en fait remplacé directement par 00000.";
                        break;
                    case "00002":
                        s = "Une erreur de cohérence est survenue.";
                        break;
                    case "00003":
                        s = "Erreur Paybox.";
                        break;
                    case "00004":
                        s = "Numéro de porteur invalide.";
                        break;
                    case "00005":
                        s = "Numéro de question invalide.";
                        break;
                    case "00006":
                        s = "Accès refusé ou site / rang incorrect.";
                        break;
                    case "00007":
                        s = "Date invalide.";
                        break;
                    case "00008":
                        s = "Date de fin de validité incorrecte.";
                        break;
                    case "00009":
                        s = "Type d'opération invalide.";
                        break;
                    case "00010":
                        s = "Devise inconnue.";
                        break;
                    case "00011":
                        s = "Montant incorrect.";
                        break;
                    case "00012":
                        s = "Référence commande invalide.";
                        break;
                    case "00013":
                        s = "Cette version n'est plus soutenue.";
                        break;
                    case "00014":
                        s = "Trame reçue incohérente.";
                        break;
                    case "00015":
                        s = "Erreur d'acces aux données precedemment referencees.";
                        break;
                    case "00016":
                        s = "Abonné déjà existant (inscription nouvel abonné).";
                        break;
                    case "00017":
                        s = "Abonné inexistant.";
                        break;
                    case "00018":
                        s = "Transaction non trouvée (question du type 11).";
                        break;
                    case "00019":
                        s = "Réservé.";
                        break;
                    case "00020":
                        s = "Cryptogramme visuel non présent.";
                        break;
                    case "00021":
                        s = "Carte non autorisée.";
                        break;
                    case "00022":
                        s = "Réservé.";
                        break;
                    case "00023":
                        s = "Réservé.";
                        break;
                    case "00024":
                        s = "Erreur de chargement de la clé : Réservé Usage Futur.";
                        break;
                    case "00025":
                        s = "Signature manquante : Réservé Usage Futur.";
                        break;
                    case "00026":
                        s = "Clé manquante mais la signature est présente : Réservé Usage Futur.";
                        break;
                    case "00027":
                        s = "Erreur OpenSSL durant la vérification de la signature : Réservé Usage Futur.";
                        break;
                    case "00028":
                        s = "Signature invalide : Réservé Usage Futur.";
                        break;
                    case "00097":
                        s = "Timeout de connexion atteint.";
                        break;
                    case "00098":
                        s = "Erreur de connexion interne.";
                        break;
                    case "00099":
                        s = "Incohérence entre la question et la réponse. Refaire une nouvelle tentative ultérieurement.";
                        break;

                    case "00151":
                        s = "Transaction refusée pour raison « provision insuffisante »,";
                        break;
                }
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return s;
        }

        /// <summary>
        /// Verifie le code retour de paybox
        /// </summary>
        /// <param name="response"></param>
        /// <returns></returns>
        private string GetResponseByCode(string codeResponse)
        {
            string s = "";
            try
            {
                switch (codeResponse)
                {
                    case "00000":
                        s = "Opération réussie.";
                        break;
                    case "00001":
                        s = "La connexion au centre d'autorisation a échouer sur les 3 urls de PayBox." +
                            "\n\r Veuillez contacter le service informatique";
                        break;
                    case "001xx":
                        s = "Paiement refusé par le centre d'autorisation." +
                            "\n\rEn cas d'autorisation de la transaction par le centre d?autorisation de la banque, le résultat 00100 sera en fait remplacé directement par 00000.";
                        break;
                    case "00002":
                        s = "Une erreur de cohérence est survenue.";
                        break;
                    case "00003":
                        s = "Erreur Paybox.";
                        break;
                    case "00004":
                        s = "Numéro de porteur invalide.";
                        break;
                    case "00005":
                        s = "Numéro de question invalide.";
                        break;
                    case "00006":
                        s = "Accès refusé ou site / rang incorrect.";
                        break;
                    case "00007":
                        s = "Date invalide.";
                        break;
                    case "00008":
                        s = "Date de fin de validité incorrecte.";
                        break;
                    case "00009":
                        s = "Type d'opération invalide.";
                        break;
                    case "00010":
                        s = "Devise inconnue.";
                        break;
                    case "00011":
                        s = "Montant incorrect.";
                        break;
                    case "00012":
                        s = "Référence commande invalide.";
                        break;
                    case "00013":
                        s = "Cette version n'est plus soutenue.";
                        break;
                    case "00014":
                        s = "Trame reçue incohérente.";
                        break;
                    case "00015":
                        s = "Erreur d'acces aux données precedemment referencees.";
                        break;
                    case "00016":
                        s = "Abonné déjà existant (inscription nouvel abonné).";
                        break;
                    case "00017":
                        s = "Abonné inexistant.";
                        break;
                    case "00018":
                        s = "Transaction non trouvée (question du type 11).";
                        break;
                    case "00019":
                        s = "Réservé.";
                        break;
                    case "00020":
                        s = "Cryptogramme visuel non présent.";
                        break;
                    case "00021":
                        s = "Carte non autorisée.";
                        break;
                    case "00022":
                        s = "Réservé.";
                        break;
                    case "00023":
                        s = "Réservé.";
                        break;
                    case "00024":
                        s = "Erreur de chargement de la clé : Réservé Usage Futur.";
                        break;
                    case "00025":
                        s = "Signature manquante : Réservé Usage Futur.";
                        break;
                    case "00026":
                        s = "Clé manquante mais la signature est présente : Réservé Usage Futur.";
                        break;
                    case "00027":
                        s = "Erreur OpenSSL durant la vérification de la signature : Réservé Usage Futur.";
                        break;
                    case "00028":
                        s = "Signature invalide : Réservé Usage Futur.";
                        break;
                    case "00097":
                        s = "Timeout de connexion atteint.";
                        break;
                    case "00098":
                        s = "Erreur de connexion interne.";
                        break;
                    case "00099":
                        s = "Incohérence entre la question et la réponse. Refaire une nouvelle tentative ultérieurement.";
                        break;

                    case "00151":
                        s = "Transaction refusée pour raison « provision insuffisante »,";
                        break;
                }
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return s;
        }

        /// <summary>
        /// Céation des données de post de Paybox 
        /// suivant le type de transaction de produit et l'agence pour le reglement
        /// </summary>
        /// <param name="banque">banque</param>
        /// <param name="payboxClient">associe du client avec le ref de paybox et par produit</param>
        /// <param name="typeTransaction">type de transaction</param>
        /// <param name="montant">montant à debiter</param>
        /// <param name="cbNumero">numero du CB</param>
        /// <param name="cbExpire">date d'expiration</param>
        /// <param name="codeSecurite">code de securité</param>
        /// <returns>les données à poste</returns>
        private NameValueCollection CreationPostData(BANQUE banque, PAYBOXCLIENT_PRODUIT payboxClient, string typeTransaction, double montant, string codeProduit, long idClient)
        {
            NameValueCollection postData = new NameValueCollection();

            try
            {
                string refCommande = ClientDAO.NextCurrentValue().ToString();
                if (payboxClient != null)
                {
                    postData.Add("NUMQUESTION", refCommande);
                    postData.Add("REFERENCE", payboxClient.CODE_PRODUIT + "_" + refCommande);
                    postData.Add("DATEQ", DateTime.Now.ToString("ddMMyyyyHHmmss"));
                    postData.Add("DEVISE", ConfigurationManager.AppSettings["codeDevisePayBox"]);
                    postData.Add("MONTANT", String.Format("{0:0000000000}", (montant * 100)));
                    postData.Add("REFABONNE", "CLI_" + payboxClient.ID_CLIENT.ToString());

                    if (!string.IsNullOrEmpty(payboxClient.REF_ABONNE))
                    {
                        var dataClient = ClientDAO.GetValueCB(payboxClient.ID_CLIENT);

                        if (dataClient != null && dataClient.Count > 0 && dataClient.Count == 3)
                        {
                            postData.Add(dataClient);
                        }
                    }

                    int i;
                    if (!string.IsNullOrEmpty(payboxClient.REF_ABONNE) && !Int32.TryParse(payboxClient.REF_ABONNE, out i))
                    {
                        if (typeTransaction.Equals("00002") || typeTransaction.Equals("00003"))
                        {
                            // force la transaction 2 en un (autorisation et debit) car le client existe déjà
                            postData.Add("TYPE", "00053");
                            if (postData.GetValues("PORTEUR")[0] != payboxClient.REF_ABONNE)
                                postData.Set("PORTEUR", payboxClient.REF_ABONNE);
                        }
                    }
                    else
                    {
                        // le client n'existe pas
                        postData.Add("TYPE", "00003");
                    }

                    #region Banque
                    if (banque != null)
                    {
                        postData.Add("SITE", banque.PAYBOX_SITE);
                        postData.Add("RANG", banque.PAYBOX_RANG);
                        postData.Add("CLE", banque.PAYBOX_CLE);
                    }
                    #endregion

                    #region valeurs par defaut
                    postData.Add("ACTIVITE", ConfigurationManager.AppSettings["activitePayBox"]);
                    postData.Add("ARCHIVAGE", "");
                    postData.Add("DIFFERE", "000");
                    postData.Add("NUMAPPEL", "");
                    postData.Add("NUMTRANS", "");
                    postData.Add("AUTORISATION", "");
                    postData.Add("PAYS", "");
                    postData.Add("VERSION", ConfigurationManager.AppSettings["paybox_version"]);
                    #endregion
                }
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }

            return postData;
        }

        /// <summary>
        /// Linéarisation des reference paybox par rapprt à un produiit données
        /// </summary>
        /// <param name="idClient"></param>
        /// <param name="banque"></param>
        /// <param name="codeproduit"></param>
        /// <returns></returns>
        private List<string> AddDeleteConsulterClientCompatibleV2(long idClient, BANQUE banque, string codeProduit, string refComnnade, string typeTransaction, long idUser, string typeCb)
        {
            List<string> response = null;
            try
            {
                var postData = new NameValueCollection();

                postData.Add("TYPE", typeTransaction);
                postData.Add("NUMQUESTION", refComnnade);
                postData.Add("REFERENCE", codeProduit + "_" + refComnnade);
                postData.Add("DATEQ", DateTime.Now.ToString("ddMMyyyyHHmmss"));
                postData.Add("MONTANT", String.Format("{0:0000000000}", (0 * 100)));
                postData.Add("DEVISE", ConfigurationManager.AppSettings["codeDevisePayBox"]);
                postData.Add("REFABONNE", "CLI_" + idClient.ToString());

                postData.Add(ClientDAO.GetValueCB(idClient));
                #region Banque
                if (banque != null)
                {
                    postData.Add("SITE", banque.PAYBOX_SITE);
                    postData.Add("RANG", banque.PAYBOX_RANG);
                    postData.Add("CLE", banque.PAYBOX_CLE);
                }
                #endregion

                #region valeurs par defaut
                postData.Add("ACTIVITE", ConfigurationManager.AppSettings["activitePayBox"]);
                postData.Add("ARCHIVAGE", "");
                postData.Add("DIFFERE", "000");
                postData.Add("NUMAPPEL", "");
                postData.Add("NUMTRANS", "");
                postData.Add("AUTORISATION", "");
                postData.Add("PAYS", "");
                postData.Add("VERSION", ConfigurationManager.AppSettings["paybox_version"]);
                #endregion

                response = this.RequestPaybox(postData);

                // ajout d'un nouveau client avec compatibilite V2
                if (response != null && response.Count > 0 && response[2].Equals("00000") && typeTransaction == "00056")
                {
                    string refClient = response[3];

                    if (!string.IsNullOrEmpty(refClient))
                    {
                        if (!ClientDAO.CheckPayBoxProduitExist(codeProduit, banque.CODE_BANQUE, idClient, refClient, typeCb))
                        {
                            var payboxProduit = new PAYBOXCLIENT_PRODUIT()
                            {
                                CODE_PRODUIT = codeProduit,
                                CODE_BANQUE = banque.CODE_BANQUE,
                                ID_CLIENT = idClient,
                                REF_ABONNE = refClient,
                                CB_TYPEID = typeCb
                            };

                            ClientDAO.AddPayBoxProduit(payboxProduit);
                        }
                    }
                }// client existe mais compatible V2
                else if (response != null && response.Count > 0 && response[2].Equals("00016") && typeTransaction == "00056")
                {
                    // recuperation du nouveau code crypté de ref abonné
                    postData.Set("TYPE", "00057");

                    response = this.RequestPaybox(postData);

                    string refClient = response[3];

                    if (!string.IsNullOrEmpty(refClient))
                    {
                        if (!ClientDAO.CheckPayBoxProduitExist(codeProduit, banque.CODE_BANQUE, idClient, refClient, typeCb))
                        {
                            var payboxProduit = new PAYBOXCLIENT_PRODUIT()
                                           {
                                               CODE_PRODUIT = codeProduit,
                                               CODE_BANQUE = banque.CODE_BANQUE,
                                               ID_CLIENT = idClient,
                                               REF_ABONNE = refClient,
                                               CB_TYPEID = typeCb
                                           };

                            ClientDAO.AddPayBoxProduit(payboxProduit);
                        }
                    }
                }// modification d'un client avec compatibilite V2
                else if (response != null && response.Count > 0 && response[2].Equals("00000") && typeTransaction == "00057")
                {
                    string refClient = response[3];

                    if (!ClientDAO.CheckPayBoxProduitExist(codeProduit, banque.CODE_BANQUE, idClient, refClient, typeCb))
                    {
                        var payboxProduit = new PAYBOXCLIENT_PRODUIT()
                                       {
                                           CODE_PRODUIT = codeProduit,
                                           CODE_BANQUE = banque.CODE_BANQUE,
                                           ID_CLIENT = idClient,
                                           REF_ABONNE = refClient,
                                           CB_TYPEID = typeCb
                                       };

                        ClientDAO.AddPayBoxProduit(payboxProduit);
                    }
                }
                this.CreatePayBoxHistorique(idClient, idUser, typeCb, banque.CODE_BANQUE, postData, response, null);
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return response;
        }
        
		/// <summary>
        /// <summary>
        /// Linéarisation des reference paybox par rapprt à un produiit données
        /// </summary>
        /// <param name="idClient"></param>
        /// <param name="banque"></param>
        /// <param name="codeproduit"></param>
        /// <returns></returns>
        private List<string> AddClientWithDebit(long idClient, BANQUE banque, double montant, string codeProduit, string refComnnade, string typeTransaction, long idUser, string typeCb)
        {
            List<string> response = null;
            try
            {
                var postData = new NameValueCollection();

                postData.Add("TYPE", typeTransaction);
                postData.Add("NUMQUESTION", refComnnade);
                postData.Add("REFERENCE", codeProduit + "_" + refComnnade);
                postData.Add("DATEQ", DateTime.Now.ToString("ddMMyyyyHHmmss"));
                postData.Add("MONTANT", String.Format("{0:0000000000}", (0.01 * 100)));
                postData.Add("DEVISE", ConfigurationManager.AppSettings["codeDevisePayBox"]);
                postData.Add("REFABONNE", "CLI_" + idClient.ToString());

                postData.Add(ClientDAO.GetValueCB(idClient));

                #region Banque
                if (banque != null)
                {
                    postData.Add("SITE", banque.PAYBOX_SITE);
                    postData.Add("RANG", banque.PAYBOX_RANG);
                    postData.Add("CLE", banque.PAYBOX_CLE);
                }
                #endregion

                #region valeurs par defaut
                postData.Add("ACTIVITE", ConfigurationManager.AppSettings["activitePayBox"]);
                postData.Add("ARCHIVAGE", "");
                postData.Add("DIFFERE", "000");
                postData.Add("NUMAPPEL", "");
                postData.Add("NUMTRANS", "");
                postData.Add("AUTORISATION", "");
                postData.Add("PAYS", "");
                postData.Add("VERSION", ConfigurationManager.AppSettings["paybox_version"]);
                #endregion

                response = this.RequestPaybox(postData);

                // ajout d'un nouveau client avec compatibilite V2
                if (response != null && response.Count > 0 && response[2].Equals("00000"))
                {
                    string refClient = response[3];

                    if (!string.IsNullOrEmpty(refClient))
                    {
                        if (!ClientDAO.CheckPayBoxProduitExist(codeProduit, banque.CODE_BANQUE, idClient, refClient, typeCb))
                        {
                            var payboxProduit = new PAYBOXCLIENT_PRODUIT()
                            {
                                CODE_PRODUIT = codeProduit,
                                CODE_BANQUE = banque.CODE_BANQUE,
                                ID_CLIENT = idClient,
                                REF_ABONNE = refClient
                            };

                            ClientDAO.AddPayBoxProduit(payboxProduit);
                        }
                    }
                }// client existe mais compatible V2
                else if (response != null && response.Count > 0 && response[2].Equals("00016") && typeTransaction == "00056")
                {
                    // recuperation du nouveau code crypté de ref abonné
                    postData.Set("TYPE", "00057");

                    response = this.RequestPaybox(postData);

                    string refClient = response[3];

                    if (!string.IsNullOrEmpty(refClient))
                    {
                        if (!ClientDAO.CheckPayBoxProduitExist(codeProduit, banque.CODE_BANQUE, idClient, refClient, typeCb))
                        {
                            var payboxProduit = new PAYBOXCLIENT_PRODUIT()
                            {
                                CODE_PRODUIT = codeProduit,
                                CODE_BANQUE = banque.CODE_BANQUE,
                                ID_CLIENT = idClient,
                                REF_ABONNE = refClient
                            };

                            ClientDAO.AddPayBoxProduit(payboxProduit);
                        }
                    }
                }// modification d'un client avec compatibilite V2
                else if (response != null && response.Count > 0 && response[2].Equals("00000") && typeTransaction == "00057")
                {
                    string refClient = response[3];

                    if (!ClientDAO.CheckPayBoxProduitExist(codeProduit, banque.CODE_BANQUE, idClient, refClient, typeCb))
                    {
                        var payboxProduit = new PAYBOXCLIENT_PRODUIT()
                        {
                            CODE_PRODUIT = codeProduit,
                            CODE_BANQUE = banque.CODE_BANQUE,
                            ID_CLIENT = idClient,
                            REF_ABONNE = refClient
                        };

                        ClientDAO.AddPayBoxProduit(payboxProduit);
                    }
                }
                this.CreatePayBoxHistorique(idClient, idUser, typeCb, banque.CODE_BANQUE, postData, response, null);
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return response;
        }

        /// <summary>
        /// Consultation de paybox par rapport à la demande et retourne une trame
        /// </summary>
        /// <param name="postaData">trame d'envoie de paybox</param>
        /// <returns>trame de reponse de paybox</returns>
        private List<string> RequestPaybox(NameValueCollection postData)
        {
            string s = "", response = "";

            List<string> trames = new List<string>();
            try
            {

                LibrairyRequest request = new LibrairyRequest(_uriPayBoxDirect1, postData);
                request.Type = PostTypeEnum.Post;

                response = request.Post();

                #region uri1
                if (!string.IsNullOrEmpty(response))
                {
                    var result = HttpUtility.ParseQueryString(response);

                    trames.Add(request.TramePost(postData));
                    trames.Add(response);

                    s = response.Substring(response.IndexOf("CODEREPONSE=") + "CODEREPONSE=".Length, 5);

                    trames.Add(s);

                    if (result != null && result.Count > 0)
                    {
                        trames[2] = result.GetValues("CODEREPONSE")[0];
                        if (!string.IsNullOrEmpty(result.GetValues("PORTEUR")[0]))
                            trames.Add(result.GetValues("PORTEUR")[0]);
                    }

                    if (!string.IsNullOrEmpty(s) && s.Equals("00001"))
                    {
                        #region uri2
                        var request2 = new LibrairyRequest(_uriPayBoxDirect2, postData);

                        request2.Url = _uriPayBoxDirect2;
                        request2.Type = PostTypeEnum.Post;
                        response = request2.Post();

                        trames[0] = request.TramePost(postData);
                        trames[1] = response;

                        s = response.Substring(response.IndexOf("CODEREPONSE=") + "CODEREPONSE=".Length, 5);
                        trames[2] = s;

                        var result2 = HttpUtility.ParseQueryString(response);
                        if (result2 != null && result2.Count > 0)
                        {
                            trames[2] = result2.GetValues("CODEREPONSE")[0];
                            if (!string.IsNullOrEmpty(result2.GetValues("PORTEUR")[0]))
                                trames.Add(result2.GetValues("PORTEUR")[0]);
                        }

                        if (!string.IsNullOrEmpty(s) && s.Equals("00001"))
                        {
                            #region uri3
                            var request3 = new LibrairyRequest(_uriPayBoxDirect3, postData);

                            request3.Url = _uriPayBoxDirect3;
                            request3.Type = PostTypeEnum.Post;
                            response = request3.Post();

                            trames[0] = request.TramePost(postData);
                            trames[1] = response;

                            s = response.Substring(response.IndexOf("CODEREPONSE=") + "CODEREPONSE=".Length, 5);

                            trames[2] = s;

                            var result3 = HttpUtility.ParseQueryString(response);
                            if (result3 != null && result3.Count > 0)
                            {
                                trames[2] = result3.GetValues("CODEREPONSE")[0];
                                if (!string.IsNullOrEmpty(result3.GetValues("PORTEUR")[0]))
                                    trames.Add(result3.GetValues("PORTEUR")[0]);
                            }

                            #endregion
                        }
                        #endregion
                    }
                }
                #endregion
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }

            return trames;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="response"></param>
        /// <returns></returns>
        private string GetRefPorteur(string response)
        {
            string s = "";
            try
            {
                if (!string.IsNullOrEmpty(response))
                {
                    s = response.Substring(response.LastIndexOf("PORTEUR=") + "PORTEUR=".Length);
                    s = s.Substring(0, s.IndexOf("&"));
                }
            }
            catch (Exception exc)
            {
                ExceptionManager.Handle(exc, ApplicationLayer.Service);
                throw;
            }
            return s;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="idClient"></param>
        /// <param name="idUser"></param>
        /// <param name="client"></param>
        /// <param name="banque"></param>
        /// <param name="postData"></param>
        /// <param name="response"></param>
        private void CreatePayBoxHistorique(long idClient, long idUser, string typeCb, string codeBanque, NameValueCollection postData, List<string> response, string idFacture)
        {
            try
            {
                var payboxHistorique = new PAYBOX_HISTO()
            {
                CB_TYPEID = typeCb,
                CODE_BANQUE = codeBanque,
                ID_CLIENT = idClient,
                ID_FACTURE = idFacture,
                PB_TRAMEQRY = response[1],
                PB_TRAMEREP = response[0],
                PB_HTTPSTATUS = (response[2] != "00001") ? "200" : response[2],
                PB_STATUS = response[2],
                OP_TYPE = postData.GetValues("TYPE")[0],
                OP_DATE = DateTime.Now.Date,
                MONTANT = Convert.ToDecimal(postData.GetValues("MONTANT")[0]) / 100,
                ID_USER = idUser,
                PB_QRYNUM = Convert.ToInt64(postData.GetValues("NUMQUESTION")[0]),
            };

                this.PayBoxHistoriqueDAO.Create(payboxHistorique);
            }
            catch (Exception ex)
            {
                ex.HelpLink = "ERROR DE CREATION DE PAYBOX HISTORIQUE";
                ExceptionManager.Handle(ex, ApplicationLayer.Service);
            }
        }

        #endregion
    }
}
