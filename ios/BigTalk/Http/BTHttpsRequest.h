//
//  BTHttpsRequest.h
//

#import <Foundation/Foundation.h>


@interface BTHttpsRequest : NSObject

+(SecIdentityRef)identityWithTrust;

+(SecIdentityRef)identityWithCert;

+(BOOL)extractIdentity:(SecIdentityRef *)outIdentity
              andTrust:(SecTrustRef *)outTrust
        fromPKCS12Data:(NSData *)inPKCS12Data;

+(BOOL)identity:(SecIdentityRef *)outIdentity
 andCertificate:(SecCertificateRef *)outCert
 fromPKCS12Data:(NSData *)inPKCS12Data;

@end


@interface BTOPURLProtocal : NSURLProtocol
{
    NSURLConnection *connection;
    NSMutableData *proRespData;
}
@end
