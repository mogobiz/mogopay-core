#!/usr/bin/env bash

curl -XDELETE 'http://localhost:12003/mogopay/PaymentRequest'

curl -XDELETE 'http://localhost:12003/mogopay/TransactionSequence'


curl -XPUT 'http://localhost:12003/mogopay/_mapping/TransactionSequence' -d '{
  "TransactionSequence": {
    "properties": {
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "transactionId": {
        "type": "long"
      },
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      }
    }
  }
}'

curl -XDELETE 'http://localhost:12003/mogopay/TransactionRequest'
curl -XPUT 'http://localhost:12003/mogopay/_mapping/TransactionRequest' -d '{
  "TransactionRequest": {
    "_ttl": {
      "enabled": true,
      "index": "not_analyzed",
      "default": "10m"
    },
    "properties": {
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "groupTransactionUUID": {
        "type": "string",
        "index": "not_analyzed"
      },
      "tid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "amount": {
        "type": "long"
      },
      "extra": {
        "type": "string",
        "index": "not_analyzed"
      },
      "currency": {
        "properties": {
          "code": {
            "type": "string",
            "index": "not_analyzed"
          },
          "fractionDigits": {
            "type": "long"
          },
          "numericCode": {
            "type": "long"
          },
          "rate": {
            "type": "double"
          }
        }
      },
      "vendorUuid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "session": {
        "type": "string",
        "index": "not_analyzed"
      },
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      }
    }
  }
}'

curl -XDELETE 'http://localhost:12003/mogopay/Country'
curl -XPUT 'http://localhost:12003/mogopay/_mapping/Country' -d '
{
  "Country": {
    "properties": {
      "billing": {
        "type": "boolean"
      },
      "code": {
        "type": "string",
        "index": "not_analyzed"
      },
      "currencyCode": {
        "type": "string",
        "index": "not_analyzed"
      },
      "currencyName": {
        "type": "string",
        "index": "not_analyzed"
      },
      "currencyNumericCode": {
        "type": "string",
        "index": "not_analyzed"
      },
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "phoneCode": {
        "type": "string",
        "index": "not_analyzed"
      },
      "shipping": {
        "type": "boolean"
      },
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "zipCodeRegex": {
        "type": "string",
        "index": "not_analyzed"
      }
    }
  }
}
'

curl -XDELETE 'http://localhost:12003/mogopay/CountryAdmin'
curl -XPUT 'http://localhost:12003/mogopay/_mapping/CountryAdmin' -d '
{
  "CountryAdmin": {
    "properties": {
      "code": {
        "type": "string",
        "index": "not_analyzed"
      },
      "country": {
        "properties": {
          "code": {
            "type": "string",
            "index": "not_analyzed"
          },
          "uuid": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      },
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "level": {
        "type": "long"
      },
      "name": {
        "type": "string",
        "index": "not_analyzed"
      },
      "parentCountryAdmin1": {
        "properties": {
          "code": {
            "type": "string",
            "index": "not_analyzed"
          },
          "uuid": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      },
      "parentCountryAdmin2": {
        "properties": {
          "code": {
            "type": "string",
            "index": "not_analyzed"
          },
          "uuid": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      },
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      }
    }
  }
}
'

curl -XDELETE 'http://localhost:12003/mogopay/Account'
curl -XPUT 'http://localhost:12003/mogopay/_mapping/Account' -d '
{
  "Account": {
    "properties": {
      "address": {
        "properties": {
          "admin1": {
            "type": "string",
            "index": "not_analyzed"
          },
          "admin2": {
            "type": "string",
            "index": "not_analyzed"
          },
          "city": {
            "type": "string",
            "index": "not_analyzed"
          },
          "civility": {
            "type": "string",
            "index": "not_analyzed"
          },
          "country": {
            "type": "string",
            "index": "not_analyzed"
          },
          "firstName": {
            "type": "string",
            "index": "not_analyzed"
          },
          "lastName": {
            "type": "string"// , Make case insensitive search work.
            //"index": "not_analyzed"
          },
          "company": {
            "type": "string",
            "index": "not_analyzed"
          },
          "road": {
            "type": "string",
            "index": "not_analyzed"
          },
          "extra": {
            "type": "string",
            "index": "not_analyzed"
          },
          "road2": {
            "type": "string",
            "index": "not_analyzed"
          },
          "telephone": {
            "properties": {
              "isoCode": {
                "type": "string",
                "index": "not_analyzed"
              },
              "lphone": {
                "type": "string",
                "index": "not_analyzed"
              },
              "phone": {
                "type": "string",
                "index": "not_analyzed"
              },
              "pinCode3": {
                "type": "string",
                "index": "not_analyzed"
              },
              "status": {
                "type": "string",
                "index": "not_analyzed"
              }
            }
          },
          "zipCode": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      },
      "birthDate": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "civility": {
        "type": "string",
        "index": "not_analyzed"
      },
      "company": {
        "type": "string",
        "index": "not_analyzed"
      },
      "country": {
        "properties": {
          "billing": {
            "type": "boolean"
          },
          "code": {
            "type": "string",
            "index": "not_analyzed"
          },
          "currencyCode": {
            "type": "string",
            "index": "not_analyzed"
          },
          "currencyName": {
            "type": "string",
            "index": "not_analyzed"
          },
          "currencyNumericCode": {
            "type": "string",
            "index": "not_analyzed"
          },
          "dateCreated": {
            "type": "date",
            "format": "dateOptionalTime"
          },
          "lastUpdated": {
            "type": "date",
            "format": "dateOptionalTime"
          },
          "name": {
            "type": "string",
            "index": "not_analyzed"
          },
          "phoneCode": {
            "type": "string",
            "index": "not_analyzed"
          },
          "shipping": {
            "type": "boolean"
          },
          "uuid": {
            "type": "string",
            "index": "not_analyzed"
          },
          "zipCodeRegex": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      },
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "email": {
        "type": "string",
        "index": "not_analyzed"
      },
      "firstName": {
        "type": "string"
      },
      "lastName": {
        "type": "string"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "loginFailedCount": {
        "type": "long"
      },
      "owner": {
        "type": "string",
        "index": "not_analyzed"
      },
      "password": {
        "type": "string",
        "index": "not_analyzed"
      },
      "paymentConfig": {
        "properties": {
          "cbParam": {
            "type": "string",
            "index": "not_analyzed"
          },
          "cbProvider": {
            "type": "string",
            "index": "not_analyzed"
          },
          "dateCreated": {
            "type": "date",
            "format":"dateOptionalTime"
          },
          "emailField": {
            "type": "string",
            "index": "not_analyzed"
          },
          "kwixoParam": {
            "type": "string",
            "index": "not_analyzed"
          },
          "lastUpdated": {
            "type": "date",
            "format":"dateOptionalTime"
          },
          "passwordField": {
            "type": "string",
            "index": "not_analyzed"
          },
          "passwordPattern": {
            "type": "string",
            "index": "not_analyzed"
          },
          "paymentMethod": {
            "type": "string",
            "index": "not_analyzed"
          },
          "paypalParam": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      },
      "roles": {
        "type": "string",
        "index": "not_analyzed"
      },
      "secret": {
        "type": "string",
        "index": "not_analyzed"
      },
      "shippingAddresses": {
        "properties": {
          "active": {
            "type": "boolean"
          },
          "uuid": {
            "type": "string",
            "index": "not_analyzed"
          },
          "address": {
            "properties": {
              "admin1": {
                "type": "string",
                "index": "not_analyzed"
              },
              "admin2": {
                "type": "string",
                "index": "not_analyzed"
              },
              "city": {
                "type": "string",
                "index": "not_analyzed"
              },
              "civility": {
                "type": "string",
                "index": "not_analyzed"
              },
              "country": {
                "type": "string",
                "index": "not_analyzed"
              },
              "firstName": {
                "type": "string",
                "index": "not_analyzed"
              },
              "lastName": {
                "type": "string",
                "index": "not_analyzed"
              },
              "road": {
                "type": "string",
                "index": "not_analyzed"
              },
              "extra": {
                "type": "string",
                "index": "not_analyzed"
              },
              "road2": {
                "type": "string",
                "index": "not_analyzed"
              },
              "telephone": {
                "properties": {
                  "isoCode": {
                    "type": "string",
                    "index": "not_analyzed"
                  },
                  "lphone": {
                    "type": "string",
                    "index": "not_analyzed"
                  },
                  "phone": {
                    "type": "string",
                    "index": "not_analyzed"
                  },
                  "pinCode3": {
                    "type": "string",
                    "index": "not_analyzed"
                  },
                  "status": {
                    "type": "string",
                    "index": "not_analyzed"
                  }
                }
              },
              "zipCode": {
                "type": "string",
                "index": "not_analyzed"
              }
            }
          }
        }
      },
      "status": {
        "type": "string",
        "index": "not_analyzed"
      },
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "waitingEmailSince": {
        "type": "long"
      },
      "waitingPhoneSince": {
        "type": "long"
      },
      "website": {
        "type": "string",
        "index": "not_analyzed"
      }
    }
  }
}
'

curl -XDELETE 'http://localhost:12003/mogopay/BOTransaction'
curl -XPUT 'http://localhost:12003/mogopay/_mapping/BOTransaction' -d '
{
  "BOTransaction": {
    "properties": {
      "amount": {
        "type": "long"
      },
      "transactionDate": {
        "type": "date",
        "format": "dateOptionalTime",
        "index": "not_analyzed"
      },
      "creditCard": {
        "properties": {
          "cardType": {
            "type": "string",
            "index": "not_analyzed"
          },
          "expiryDate": {
            "type": "date",
            "index": "no"
          },
          "holder": {
            "type": "string",
            "index": "not_analyzed"
          },
          "number": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      },
      "currency": {
        "properties": {
          "code": {
            "type": "string",
            "index": "not_analyzed"
          },
          "fractionDigits": {
            "type": "long"
          },
          "numericCode": {
            "type": "long"
          },
          "rate": {
            "type": "double"
          }
        }
      },
      "customer": {
        "properties": {
          "address": {
            "properties": {
              "city": {
                "type": "string",
                "index": "not_analyzed"
              },
              "country": {
                "type": "string",
                "index": "not_analyzed"
              },
              "road": {
                "type": "string",
                "index": "not_analyzed"
              },
              "zipCode": {
                "type": "string",
                "index": "not_analyzed"
              }
            }
          },
          "civility": {
            "type": "string",
            "index": "not_analyzed"
          },
          "company": {
            "type": "string",
            "index": "not_analyzed"
          },
          "dateCreated": {
            "type": "date",
            "format": "dateOptionalTime"
          },
          "email": {
            "type": "string",
            "index": "not_analyzed"
          },
          "firstName": {
            "type": "string",
            "index": "not_analyzed"
          },
          "lastName": {
            "type": "string",
            "index": "not_analyzed"
          },
          "lastUpdated": {
            "type": "date",
            "format": "dateOptionalTime"
          },
          "loginFailedCount": {
            "type": "long"
          },
          "owner": {
            "type": "string",
            "index": "not_analyzed"
          },
          "password": {
            "type": "string",
            "index": "not_analyzed"
          },
          "roles": {
            "type": "string",
            "index": "not_analyzed"
          },
          "status": {
            "type": "string",
            "index": "not_analyzed"
          },
          "uuid": {
            "type": "string",
            "index": "not_analyzed"
          },
          "waitingEmailSince": {
            "type": "long"
          },
          "waitingPhoneSince": {
            "type": "long"
          },
          "website": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      },
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "email": {
        "type": "string",
        "index": "not_analyzed"
      },
      "endDate": {
        "type": "date",
        "index": "no"
      },
      "errorCodeOrigin": {
        "type": "string",
        "index": "not_analyzed"
      },
      "errorMessageOrigin": {
        "type": "string",
        "index": "not_analyzed"
      },
      "shippingInfo": {
        "type": "string",
        "index": "not_analyzed"
      },
      "shippingTrackingNumber": {
        "type": "string",
        "index": "not_analyzed"
      },
      "extra": {
        "type": "string",
        "index": "not_analyzed"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "merchantConfirmation": {
        "type": "boolean"
      },
      "paymentData": {
        "properties": {
          "authorizationId": {
            "type": "string",
            "index": "not_analyzed"
          },
          "cbProvider": {
            "type": "string",
            "index": "not_analyzed"
          },
          "dateCommandCB": {
            "type": "long",
            "index": "no"
          },
          "idCommandCB": {
            "type": "long"
          },
          "paymentType": {
            "type": "string",
            "index": "not_analyzed"
          },
          "status3DS": {
            "type": "string",
            "index": "not_analyzed"
          },
          "transactionDate": {
            "type": "date",
            "index": "no"
          },
          "transactionId": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      },
      "status": {
        "type": "string",
        "index": "not_analyzed"
      },
      "groupTransactionUUID": {
        "type": "string",
        "index": "not_analyzed"
      },
      "transactionUUID": {
        "type": "string",
        "index": "not_analyzed"
      },
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "vendor": {
        "properties": {
          "address": {
            "properties": {
              "city": {
                "type": "string",
                "index": "not_analyzed"
              },
              "country": {
                "type": "string",
                "index": "not_analyzed"
              },
              "road": {
                "type": "string",
                "index": "not_analyzed"
              },
              "zipCode": {
                "type": "string",
                "index": "not_analyzed"
              }
            }
          },
          "civility": {
            "type": "string",
            "index": "not_analyzed"
          },
          "company": {
            "type": "string",
            "index": "not_analyzed"
          },
          "dateCreated": {
            "type": "date",
            "format": "dateOptionalTime"
          },
          "email": {
            "type": "string",
            "index": "not_analyzed"
          },
          "firstName": {
            "type": "string",
            "index": "not_analyzed"
          },
          "lastName": {
            "type": "string",
            "index": "not_analyzed"
          },
          "lastUpdated": {
            "type": "date",
            "format": "dateOptionalTime"
          },
          "loginFailedCount": {
            "type": "long"
          },
          "password": {
            "type": "string",
            "index": "not_analyzed"
          },
          "paymentConfig": {
            "properties": {
              "cbParam": {
                "type": "string",
                "index": "not_analyzed"
              },
              "cbProvider": {
                "type": "string",
                "index": "not_analyzed"
              },
              "dateCreated": {
                "type": "date",
                "format": "dateOptionalTime"
              },
              "emailField": {
                "type": "string",
                "index": "not_analyzed"
              },
              "kwixoParam": {
                "type": "string",
                "index": "not_analyzed"
              },
              "lastUpdated": {
                "type": "date",
                "format": "dateOptionalTime"
              },
              "passwordField": {
                "type": "string",
                "index": "not_analyzed"
              },
              "passwordPattern": {
                "type": "string",
                "index": "not_analyzed"
              },
              "paymentMethod": {
                "type": "string",
                "index": "not_analyzed"
              },
              "paypalParam": {
                "type": "string",
                "index": "not_analyzed"
              }
            }
          },
          "roles": {
            "type": "string",
            "index": "not_analyzed"
          },
          "status": {
            "type": "string",
            "index": "not_analyzed"
          },
          "uuid": {
            "type": "string",
            "index": "not_analyzed"
          },
          "waitingEmailSince": {
            "type": "long"
          },
          "waitingPhoneSince": {
            "type": "long"
          },
          "website": {
            "type": "string",
            "index": "not_analyzed"
          }
        }
      }
    }
  }
}
'

curl -XDELETE 'http://localhost:12003/mogopay/BOTransactionLog'
curl -XPUT 'http://localhost:12003/mogopay/_mapping/BOTransactionLog' -d '
{
  "BOTransactionLog": {
    "properties": {
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "direction": {
        "type": "string",
        "index": "not_analyzed"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "log": {
        "type": "string",
        "index": "not_analyzed"
      },
      "provider": {
        "type": "string",
        "index": "not_analyzed"
      },
      "transaction": {
        "type": "string",
        "index": "not_analyzed"
      },
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "step": {
        "type": "string",
        "index": "not_analyzed"
      }
    }
  }
}
'


curl -XDELETE 'http://localhost:12003/mogopay/Rate'
curl -XPUT 'http://localhost:12003/mogopay/_mapping/Rate' -d '
{
  "Rate": {
    "properties": {
      "currencyCode": {
        "type": "string",
        "index": "not_analyzed"
      },
      "activationDate": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "currencyRate": {
        "type": "double"
      },
      "currencyFractionDigits": {
        "type": "integer"
      },
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      }
    }
  }
}'



curl -XDELETE 'http://localhost:12003/mogopay/TransactionRequest'
curl -XPUT 'http://localhost:12003/mogopay/_mapping/TransactionRequest' -d '
{
  "TransactionRequest": {
    "_ttl": {
      "enabled": true,
      "index": "not_analyzed",
      "default": "{{ttl}}"
    },
    "properties": {
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "groupTransactionUUID": {
        "type": "string",
        "index": "not_analyzed"
      },
      "tid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "amount": {
        "type": "long"
      },
      "extra": {
        "type": "string",
        "index": "not_analyzed"
      },
      "currency": {
        "properties": {
          "code": {
            "type": "string",
            "index": "not_analyzed"
          },
          "fractionDigits": {
            "type": "long"
          },
          "numericCode": {
            "type": "long"
          },
          "rate": {
            "type": "double"
          }
        }
      },
      "vendorUuid": {
        "type": "string",
        "index": "not_analyzed"
      },
      "session": {
        "type": "string",
        "index": "not_analyzed"
      },
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      }
    }
  }
}'



curl -XDELETE 'http://localhost:12003/mogopay/TransactionSequence'
curl -XPUT 'http://localhost:12003/mogopay/_mapping/TransactionSequence' -d '
{
  "TransactionSequence": {
    "properties": {
      "dateCreated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "lastUpdated": {
        "type": "date",
        "format": "dateOptionalTime"
      },
      "transactionId": {
        "type": "long"
      },
      "uuid": {
        "type": "string",
        "index": "not_analyzed"
      }
    }
  }
}
'