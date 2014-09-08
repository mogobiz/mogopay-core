package mogopay.handlers.payment

object BankErrorCodes {
  def getErrorMessage(code: String): String = errorMessages(code)

  private val errorMessages: Map[String, String] = Map(
    "" -> "",
    "00" -> "Transaction approuvée ou traitée avec succès",
    "02" -> "Contacter l'émetteur de carte",
    "03" -> "Accepteur invalide",
    "04" -> "Conserver la carte",
    "05" -> "Ne pas honorer",
    "07" -> "Conserver la carte, conditions spéciales",
    "08" -> "Approuver après identification",
    "12" -> "Transaction invalide",
    "13" -> "Montant invalide",
    "14" -> "Numéro de porteur invalide",
    "15" -> "Emetteur de carte inconnu",
    "30" -> "Erreur de format",
    "31" -> "Identifiant de l'organisme acquéreur inconnu",
    "33" -> "Date de validité de la carte dépassée",
    "34" -> "Suspicion de fraude",
    "41" -> "Carte perdue",
    "43" -> "Carte volée",
    "51" -> "Provision insuffisante ou crédit dépassé",
    "54" -> "Date de validité de la carte dépassée",
    "56" -> "Carte absente du fichier",
    "57" -> "Transaction non permise à ce porteur",
    "58" -> "Transaction interdite au terminal",
    "59" -> "Suspicion de fraude",
    "60" -> "L'accepteur de carte doit contacter l'acquéreur",
    "61" -> "Dépasse la limite du montant de retrait",
    "63" -> "Règles de sécurité non respectées",
    "68" -> "Réponse non parvenue ou reçue trop tard",
    "90" -> "Arrêt momentané du système",
    "91" -> "Emetteur de cartes inaccessible",
    "96" -> "Mauvais fonctionnement du système",
    "97" -> "Échéance de la temporisation de surveillance globale",
    "98" -> "Serveur indisponible routage réseau demandé à nouveau",
    "99" -> "Incident domaine initiateur"
  )
}