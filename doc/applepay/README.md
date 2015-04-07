# Using Apple Pay with Mogopay
## Configuration
Please read 
[http://www.authorize.net/support/ApplePay_Getting_Started.pdf](http://www.authorize.net/support/ApplePay_Getting_Started.pdf) for all the details regarding setting up and configuring Apple Pay with Authorize.net.

## Usage
### Transactions
```
   Buyer                   Mogopay                  Merchant                 Authorize.net
    +                         +                         +                         +            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    | (1) Cart checkout       |                         |                         |            
    | +-----------------------------------------------> |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |           (2) /pay/init |                         |            
    |                         | <---------------------+ |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |   (3) checkout response |                         |            
    | <-----------------------------------------------+ |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    | (4) /pay/submit         |                         |                         |            
    | +---------------------> |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         | (5) payment request     |                         |            
    |                         | +-----------------------------------------------> |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         | (6) payment req. resp.  |            
    |                         | <-----------------------------------------------+ |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         | (7) Call success_url    |                         |            
    |                         | +---------------------> |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |       (8) Send response |                         |            
    |                         | <---------------------+ |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    |                         |                         |                         |            
    | (9) Transfer the resp.  |                         |                         |            
    | <---------------------+ |                         |                         |            
    +                         +                         +                         +            
```

### Special notes
* In `/pay/submit` (step 4) make sure you specify these parameters:
  * `success_url`: This URL would be called in step 7, and its result would be passed to the iOS app with no additional treatment (step 8 & 9).
The usage you'd make of the result is up to you. In the sample app we simply print it in an alert box.
  * `gateway_data`: must contain the Apple Pay token (from `PKPayment`).

## Handling errors
There are 3 kinds of possible errors:

* Connexion to Authorize.net failed: will return a 500 code as well as a JSON object containing `NoResponseFromAuthorizeNetException` in the `type` field.  
* Authorize.net returned an error code: will return a 500 code as well as a JSON object containing `AuthorizeNetErrorException` in the `type` field and Authorize.net's code in the `error` field.  
* No success URL provided:  will return a 500 code as well as a JSON object containing `NoSuccessURLProvided` in the `type` field.

## More
* Interesting reading : [https://developer.apple.com/apple-pay/Getting-Started-with-Apple-Pay.pdf](https://developer.apple.com/apple-pay/Getting-Started-with-Apple-Pay.pdf).