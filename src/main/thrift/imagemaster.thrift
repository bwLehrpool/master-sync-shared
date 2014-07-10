/**
 * Define some namespace/package name for our stuff
 */

namespace java org.openslx.imagemaster.thrift.iface
namespace php testing

typedef string ID
typedef string Token
typedef string UUID
typedef i64 Date

enum AuthorizationError {
	GENERIC_ERROR,
	NOT_AUTHENTICATED,
	NO_PERMISSION
}

enum AuthenticationError {
	GENERIC_ERROR,
	INVALID_CREDENTIALS,
	ACCOUNT_SUSPENDED,
	BANNED_NETWORK
}

enum ServerAuthenticationError {
	GENERIC_ERROR,
	INVALID_ORGANIZATION,
	BANNED_NETWORK
}

enum ImageDataError {
	INVALID_DATA,
	UNKNOWN_IMAGE
}

enum UploadError {
	INVALID_CRC
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

exception ServerAuthenticationException {
	1: ServerAuthenticationError number,
	2: string message
}

exception ImageDataException {
	1: ImageDataError number,
	2: string message
}

exception UploadException {
	1: UploadError numberm
	2: string message
}

struct UserInfo {
	1: string userId,
	2: string firstName,
	3: string lastName,
	4: string eMail
}

struct SessionData {
	1: ID sessionId,
	2: Token authToken,
	3: string serverAddress
}

struct FtpCredentials {
	1: string username,
	2: string password,
	3: string filename
}

struct UploadInfos {
	1: string token,
	2: i32 port,
	3: list<i32> missingBlocks
}

struct DownloadInfos {
	1: string token,
	2: i32 port
}

struct ServerSessionData {
	1: ID sessionId
}

struct ImageData {
	1: UUID uuid,
	2: i32 imageVersion,
	3: string imageName,
	4: Date imageCreateTime,
	5: Date imageUpdateTime,
	6: string imageOwner,
	7: string conentOperatingSystem,
	8: bool statusIsValid,
	9: bool statusIsDeleted,
	10: string imageShortDescription,
	11: string imageLongDescription,
	12: i64 fileSize
}

service ImageServer {

	bool ping(),

	SessionData authenticate(1:string username, 2:string password) throws (1:AuthenticationException failure),

	UserInfo getUserFromToken(1:Token token) throws (1:InvalidTokenException failure),
	
	string startServerAuthentication(1:string organization) throws (1: ServerAuthenticationException failure),
	
	bool isServerAuthenticated(1:string serverSessionId),
	
	ServerSessionData serverAuthenticate(1:string organization, 2:binary challengeResponse) throws (1:ServerAuthenticationException failure),
	
	UploadInfos submitImage(1:string serverSessionId, 2:ImageData imageDescription, 3:list<i32> crcSums) throws (1:AuthorizationException failure, 2: ImageDataException failure2, 3: UploadException failure3),
	
	DownloadInfos getImage(1:UUID uuid, 2:string serverSessionId, 3:list<i32> requestedBlocks) throws (1:AuthorizationException failure, 2: ImageDataException failure2),

}