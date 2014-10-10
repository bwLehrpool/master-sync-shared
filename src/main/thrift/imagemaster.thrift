/**
 * Define some namespace/package name for our stuff
 */

namespace java org.openslx.imagemaster.thrift.iface
namespace php testing

typedef string ID
typedef string Token
typedef string UUID
typedef i64 UnixTimestamp

enum AuthorizationError {
	GENERIC_ERROR,
	NOT_AUTHENTICATED,
	NO_PERMISSION
}

enum AuthenticationError {
	GENERIC_ERROR,
	INVALID_CREDENTIALS,
	ACCOUNT_SUSPENDED,
	INVALID_ORGANIZATION,
	INVALID_KEY,
	CHALLENGE_FAILED,
	BANNED_NETWORK
}

enum ImageDataError {
	INVALID_DATA,
	UNKNOWN_IMAGE
}

enum UploadError {
	INVALID_CRC,
	BROKEN_BLOCK,
	GENERIC_ERROR,
	INVALID_METADATA,
	ALREADY_COMPLETE
}

exception AuthorizationException {
	1: AuthorizationError number,
	2: string message
}

exception AuthenticationException {
	1: AuthenticationError number,
	2: string message
}

exception InvalidTokenException {
}

exception ImageDataException {
	1: ImageDataError number,
	2: string message
}

exception UploadException {
	1: UploadError number,
	2: string message
}

exception DownloadException {
	1: UploadError number,
	2: string message
}

struct UserInfo {
	1: string userId,
	2: string firstName,
	3: string lastName,
	4: string eMail,
	5: string organizationId
}

struct OrganizationData {
	1: string organizationId,
	2: string displayName,
	3: string ecpUrl,
}

struct SessionData {
	1: ID sessionId,
	2: Token authToken,
	3: string serverAddress
}

struct UploadData {
	1: string token,
	2: i32 port
}

struct DownloadData {
	1: string token,
	2: i32 port,
	3: list<i32> crcSums
}

struct ServerSessionData {
	1: ID sessionId
}

struct ImageData {
	1: UUID uuid,
	2: i32 revision,
	3: string title,
	4: UnixTimestamp createTime,
	5: UnixTimestamp updateTime,
	6: string ownerLogin,
	7: i32 operatingSystem,
	8: bool isValid,
	9: bool isDeleted,
	// 10: deleted, do not reuse!
	11: string description,
	12: i64 fileSize,
}

service ImageServer {

/*
 * Client calls
 */
	bool ping(),

	SessionData authenticate(1:string login, 2:string password) throws (1:AuthenticationException failure),
	
	list<OrganizationData> getOrganizations(),
	
	list<UserInfo> findUser(1:ID sessionId, 2:string organizationId, 3:string searchTerm) throws (1:AuthorizationException failure),
	
	list<ImageData> getPublicImages(1:ID sessionId, 2:i32 page) throws (1:AuthorizationException failure),

/*
 * Server calls
 */
	UserInfo getUserFromToken(1:Token token) throws (1:InvalidTokenException failure),
	
	bool isServerAuthenticated(1:string serverSessionId),
	
	binary startServerAuthentication(1:string organization) throws (1: AuthenticationException failure),
	
	ServerSessionData serverAuthenticate(1:string organizationId, 2:binary challengeResponse) throws (1:AuthenticationException failure),
	
	UploadData submitImage(1:ID serverSessionId, 2:ImageData imageDescription, 3:list<i32> crcSums) throws (1:AuthorizationException failure, 2: ImageDataException failure2, 3: UploadException failure3),
	
	DownloadData getImage(2:ID serverSessionId, 1:UUID uuid) throws (1:AuthorizationException failure, 2: ImageDataException failure2),
	
	bool publishUser(1:ID serverSessionId, 2:UserInfo user) throws (1:AuthorizationException failure),

	bool registerSatellite(1:string organizationId, 2:string address, 3:string modulus, 4:string exponent),

}
