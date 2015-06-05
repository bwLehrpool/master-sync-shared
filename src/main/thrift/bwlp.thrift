/**
 * Define some namespace/package name for our stuff
 */
namespace java org.openslx.bwlp.thrift.iface
 
typedef i64 int

typedef string ID
typedef string Token
typedef string UUID
typedef string UserID
typedef i64 UnixTimestamp

// ################# ENUM ################

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

// ############## STRUCT ###############

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
	4: list<string> suffixList,
}

struct SessionData {
	1: ID sessionId,
	2: Token authToken,
	3: string serverAddress
}

struct ServerSessionData {
	1: ID sessionId
}

struct OperatingSystem {
	1: i32 osId,
	2: string osName,
	3: map<string, string> virtualizerOsId,
	4: string architecture,
}

struct ImagePermission {
	1: bool read
	2: bool write
	3: bool admin
	4: bool link
}

struct LecturePermission {
	1: bool write
	2: bool admin
}

struct ImageBaseData {
	1: UUID imageBaseId,
	3: string imageName,
   10: string description,
	5: i32 osId,
	6: UnixTimestamp createTime,
	7: UnixTimestamp updateTime,
	9: string isTemplate,
}
struct ImagePermissionCombined {
	1: Image image,
	2: ImagePermission permission,
}

struct Lecture {
	1: optional UUID lectureId,
	2: string lectureName,
	3: string isActive,
	4: UnixTimestamp startTime,
	5: UnixTimestamp endTime,
	6: UnixTimestamp lastUsed,
	7: string description,
	8: UUID imageId
}
struct LecturePermissionCombined {
	1: Lecture lecture,
	2: LecturePermission permission,
}

struct TransferInformation {
	1: string token,
	2: i32 plainPort,
	3: i32 sslPort,
}

// ############ EXCEPTION ######################

exception TUploadFinishException {
	1: string reason,
}

exception TUploadRejectedException {
	1: string reason,
}

exception TAuthorizationException {
	1: AuthorizationError number,
	2: string message
}

exception TAuthenticationException {
	1: AuthenticationError number,
	2: string message
}

exception TInvalidTokenException {
}

exception TImageDataException {
	1: ImageDataError number,
	2: string message
}

exception TDownloadRejectedException {
	1: UploadError number,
	2: string message
}

// #############################################

service SatelliteServer {
	int getVersion(),
	
	// File transfer related
	TransferInformation requestUpload(1: string userToken, 2: i64 fileSize, 3: list<binary> blockHashes)
		throws (1:TUploadRejectedException rejection),
	void cancelUpload(1: string uploadToken),
	TransferInformation requestDownload(1: string userToken, 2: string imageId),
	void cancelDownload(1: string downloadToken),
	
	// Authentication
	bool authenticated(1: Token userToken),
	bool setSessionInvalid(1: Token userToken),
	
	// Misc
    list<OperatingSystem> getOperatingSystems(),
    list<string> getAllOrganizations(1: Token userToken),
	
	// Image related
    string finishImageUpload(1: string imageName, 2: string description, 10: bool license, 11: bool internet, 17: int shareMode, 18: string os, 22: Token uploadToken)
    	throws (1:TUploadFinishException failure),
    list<ImagePermissionCombined> getImageList(1: Token userToken),
	ImagePermissionCombined getImageData(1: UUID imageId, 3: Token userToken),
	bool updateImageData(1: Token userToken, 9: Image image),
	
	// Lecture related
    bool writeLecture(1: Token userToken, 2: Lecture lecture),
    list<Lecture> getLectureList(1: Token userToken),
	Lecture getLectureData(1: UUID lectureId, 2: Token userToken),
	bool deleteImage(1: string id, 2: string version, 3: string token),
	bool connectedToLecture(1: string id, 2: string version, 3: string token),
	bool deleteLecture(1: string lectureId, 2: string token),
	bool checkUser(1: string username, 2: string token),
	bool writeImagePermissions(1: Token userToken, 2: UUID imageId, 3: list<ImagePermission> permissions),
	bool writeLecturePermissions(1: Token userToken, 2: UUID lectureId, 3: list<LecturePermission> permissions),
	map<UserID, UserInfo> getImagePermissions(1: Token userToken, 2: UUID imageId),
	map<UserID, UserInfo> getLecturePermissions(1: Token userToken, 2: UUID lectureId),
	void deleteAllAdditionalImagePermissions(1: UUID imageID, 2: Token userToken),
	void deleteAllAdditionalLecturePermissions(1: UUID lectureID, 2: Token userToken),
}

// Central master server

service MasterServer {

/*
 * Client (User's Desktop App) calls
 */
	bool ping(),

	SessionData authenticate(1:string login, 2:string password) throws (1:AuthenticationException failure),
	
	list<OrganizationData> getOrganizations(),
	
	list<UserInfo> findUser(1:ID sessionId, 2:string organizationId, 3:string searchTerm) throws (1:AuthorizationException failure),
	
	list<ImageData> getPublicImages(1:ID sessionId, 2:i32 page) throws (1:AuthorizationException failure),
	
	list<OperatingSystem> getOperatingSystems(),

/*
 * Server (Satellite) calls
 */
	UserInfo getUserFromToken(1:Token token) throws (1:InvalidTokenException failure),
	
	bool isServerAuthenticated(1:string serverSessionId),
	
	binary startServerAuthentication(1:string organization) throws (1: AuthenticationException failure),
	
	ServerSessionData serverAuthenticate(1:string organizationId, 2:binary challengeResponse) throws (1:AuthenticationException failure),
	
	UploadData submitImage(1:ID serverSessionId, 2:ImageData imageDescription, 3:list<i32> crcSums) throws (1:AuthorizationException failure, 2: ImageDataException failure2, 3: UploadException failure3),
	
	DownloadData getImage(2:ID serverSessionId, 1:UUID uuid) throws (1:AuthorizationException failure, 2: ImageDataException failure2),
	
	bool publishUser(1:ID serverSessionId, 2:UserInfo user) throws (1:AuthorizationException failure),

	bool registerSatellite(1:string organizationId, 2:string address, 3:string modulus, 4:string exponent),

	bool updateSatelliteAddress(1:ID serverSessionId, 2:string address),

}
