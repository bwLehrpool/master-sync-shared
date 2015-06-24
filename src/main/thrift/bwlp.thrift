/**
 * Define some namespace/package name for our stuff
 */
namespace java org.openslx.bwlp.thrift.iface
 
typedef i64 int

// Use these typedefs if appropriate (to clarify)
typedef string Token
typedef string UUID
typedef i64 UnixTimestamp

// Use *only* strings (or typedefs of string) as keys for a map (map<string, ...>)
// UpperCamelCase for struct names
// lowerCamelCase for methods, variables, struct members
// CAPS_WITH_UNDERSCORE for enum members 

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

enum ShareMode {
	LOCAL,
	PUBLISH,
	DOWNLOAD
}

enum NetDirection {
	IN,
	OUT
}

enum Role {
	STUDENT,
	TUTOR
}

// ############## STRUCT ###############

struct UserInfo {
	1: string userId,
	2: string firstName,
	3: string lastName,
	4: string eMail,
	5: string organizationId,
	6: optional Role role,
}

struct Organization {
	1: string organizationId,
	2: string displayName,
	3: string ecpUrl,
	4: list<string> suffixList,
}

struct Satellite {
	1: list<string> addressList,
	2: string displayName
}

// Old session information for clients logging in via "test-account"
struct SessionData {
	1: Token sessionId,
	2: Token authToken,
	3: string serverAddress
}

struct ClientSessionData {
	1: Token sessionId,
	2: Token authToken,
	3: list<Satellite> satellites
}

struct ServerSessionData {
	1: Token sessionId
}

struct Virtualizer {
	1: string virtId,
	2: string virtName
}

struct OperatingSystem {
	1: i32 osId,
	2: string osName,
	3: map<string, string> virtualizerOsId,
	4: string architecture,
}

// Sat <-> Master
struct ImagePublishData {
	1: UUID imageBaseId,
	2: UUID currentVersionId,
	3: string imageName,
	4: i32 osId,
	5: string virtId,
	6: UnixTimestamp baseCreateTime,
	7: UnixTimestamp versionCreateTime,
	8: UUID ownerId,
	9: UUID uploaderId,
	11: i64 fileSize,
	16: bool isTemplate,
}

struct ImagePermissions {
	1: bool link
	2: bool download
	3: bool edit
	4: bool admin
}

struct LecturePermissions {
	1: bool edit
	2: bool admin
}

struct ImageBaseWrite {
	1: string imageName,
    2: string description,
	3: i32 osId,
	4: string virtId,
	5: bool isTemplate,
	6: ImagePermissions defaultPermissions,
	7: ShareMode shareMode,
}

struct ImageVersionWrite {
	1: bool isEnabled,
	2: bool isRestricted,
}

struct ImageSummaryRead {
	1: UUID imageBaseId,
	2: UUID currentVersionId,
	3: UUID latestVersionId,
	4: string imageName,
	5: i32 osId,
	6: string virtId,
	7: UnixTimestamp createTime,
	8: UnixTimestamp updateTime,
	9: UnixTimestamp expireTime,
	10: UUID ownerId,
	11: UUID uploaderId,
	12: ShareMode shareMode,
	13: i64 fileSize,
	14: bool isRestricted,
	15: bool isValid,
	16: bool isProcessed,
	17: bool isTemplate,
	18: ImagePermissions defaultPermissions,
	19: optional ImagePermissions userPermissions,
}

struct ImageVersionDetails {
	1: UUID versionId,
	2: UnixTimestamp createTime,
	3: UnixTimestamp expireTime,
	4: i64 fileSize,
	5: UUID uploaderId,
	6: bool isEnabled,
	7: bool isRestricted,
	8: bool isValid,
	9: bool isProcessed,
	10: list<string> software,
}

struct ImageDetailsRead {
	1: UUID imageBaseId,
	2: UUID currentVersionId,
	17: UUID latestVersionId,
	3: list<ImageVersionDetails> versions,
	4: string imageName,
	5: string description,
	6: list<string> tags,
	8: i32 osId,
	9: string virtId,
	10: UnixTimestamp createTime,
	11: UnixTimestamp updateTime,
	12: UUID ownerId,
	13: UUID updaterId,
	14: ShareMode shareMode,
	15: bool isTemplate,
	16: ImagePermissions defaultPermissions,
	18: optional ImagePermissions userPermissions,
}

struct NetRule {
	1: i32 ruleId,
	2: NetDirection direction,
	3: string host,
	4: i32 port,
}

// Write lecture to sat. if optional fields are not set or null, their value stays unchanged
struct LectureWrite {
	1: string lectureName,
	2: string description,
	3: UUID imageVersionId,
	4: bool autoUpdate,
	5: bool isEnabled,
	6: UnixTimestamp startTime,
	7: UnixTimestamp endTime,
	9: string runscript,
	10: list<string> nics,
	11: optional list<string> allowedUsers, // allowed to see/select image in vmchooser. These are local accounts, not bwIDM/Master
	12: optional list<NetRule> networkExceptions,
	13: bool isExam,
	14: bool hasInternetAccess,
	15: LecturePermissions defaultPermissions,
}

struct LectureSummary {
	1: UUID lectureId,
	2: string lectureName,
	3: UUID imageVersionId,
	4: string imageName,
	5: bool isEnabled,
	6: UnixTimestamp startTime,
	7: UnixTimestamp endTime,
	8: UnixTimestamp lastUsed,
	9: i32 useCount,
	10: UUID ownerId,
	11: UUID updaterId,
	12: bool isExam,
	13: bool hasInternetAccess,
	14: LecturePermissions defaultPermissions,
	15: optional LecturePermissions userPermissions,
	16: bool isImageVersionUsable, // Is the linked image version valid and enabled?
}

struct LectureRead {
	1: UUID lectureId,
	2: string lectureName,
	3: string description,
	4: ImageSummaryRead image,
	5: bool autoUpdate,
	6: bool isEnabled,
	7: UnixTimestamp startTime,
	8: UnixTimestamp endTime,
	9: UnixTimestamp lastUsed,
	10: i32 useCount,
	11: UUID ownerId,
	12: UUID updaterId,
	13: string runscript,
	14: list<string> nics,
	15: list<string> allowedUsers, // allowed to see/select image in vmchooser. These are local accounts, not bwIDM/Master
	16: list<NetRule> networkExceptions,
	17: bool isExam,
	18: bool hasInternetAccess,
	19: LecturePermissions defaultPermissions,
	20: optional LecturePermissions userPermissions,
}

struct TransferInformation {
	1: string token,
	2: i32 plainPort,
	3: i32 sslPort,
}

// Used to tell status of an upload. The blockStatus is one byte per 16MB block,
// 0 = complete, 1 = missing, 2 = uploading, 3 = queued for copying, 4 = copying 
struct UploadStatus {
	1: binary blockStatus,
}

// ############ EXCEPTION ######################

exception TTransferRejectedException {
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

exception TNotFoundException {
}

exception TInternalServerError {
}

exception TImageDataException {
	1: ImageDataError number,
	2: string message
}

// #############################################

service SatelliteServer {
	int getVersion(),
	
	// File transfer related
	TransferInformation requestImageVersionUpload(1: Token userToken, 2: UUID imageBaseId, 3: i64 fileSize, 4: list<binary> blockHashes)
		throws (1:TTransferRejectedException rejection, 2:TAuthorizationException authError),
	void cancelUpload(1: Token uploadToken),
	UploadStatus queryUploadStatus(1: Token uploadToken)
		throws (1:TInvalidTokenException ex1),

	TransferInformation requestDownload(1: Token userToken, 2: UUID imageVersionId)
		throws (1:TAuthorizationException authError),
	void cancelDownload(1: string downloadToken),
	
	// Authentication
	bool isAuthenticated(1: Token userToken),
	void invalidateSession(1: Token userToken),
	
	// find a user in a given organization by a search term
	list<UserInfo> getUserList(1:Token userToken, 2:i32 page)
		throws (1:TAuthorizationException failure, 2:TInternalServerError serverError),
	
	// Misc
    list<OperatingSystem> getOperatingSystems(),
	list<Virtualizer> getVirtualizers(),
    list<Organization> getAllOrganizations(),
	
	/*
	 * Image related
	 */
	// Get image list. tagSearch can be null, which disables this type of filtering and returns all
    list<ImageSummaryRead> getImageList(1: Token userToken, 2: list<string> tagSearch)
		throws (1:TAuthorizationException authError, 2:TInternalServerError serverError),
	// Query detailed information about an image
	ImageDetailsRead getImageDetails(1: Token userToken, 2: UUID imageBaseId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Create a new image; the image will have no versions, so the user needs to upload one and set meta data later on
	UUID createImage(1: Token userToken, 2: string imageName)
		throws (1:TAuthorizationException authError, 2:TImageDataException imgError, 3:TInternalServerError serverError),
	// Update given image's base meta data
	void updateImageBase(1: Token userToken, 2: UUID imageBaseId 3: ImageBaseWrite image)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TImageDataException imgError, 4:TInternalServerError serverError),
	// Update a certain image version's meta data
	void updateImageVersion(1: Token userToken, 2: UUID imageVersionId 3: ImageVersionWrite image)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TImageDataException imgError, 4:TInternalServerError serverError),
	// Delete given image version. If the version is currently in use by a lecture, it will not be
	// deleted and false is returned
	void deleteImageVersion(1: Token userToken, 2: UUID imageVersionId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Write list of permissions for given image 
	void writeImagePermissions(1: Token userToken, 2: UUID imageBaseId, 3: map<UUID, ImagePermissions> permissions)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Get all user-permissions for given image 
	map<UUID, ImagePermissions> getImagePermissions(1: Token userToken, 2: UUID imageBaseId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Set new owner of image
	void setImageOwner(1: Token userToken, 2: UUID imageBaseId 3: UUID newOwnerId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	
	/*
	 * Lecture related
	 */
	// Create new lecture
    UUID createLecture(1: Token userToken, 2: LectureWrite lecture)
		throws (1:TAuthorizationException authError, 2:TInternalServerError serverError),
	// Update existing lecture
    void updateLecture(1: Token userToken, 2: UUID lectureId, 3: LectureWrite lecture)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Get list of all lectures
    list<LectureSummary> getLectureList(1: Token userToken)
		throws (1:TAuthorizationException authError, 2:TInternalServerError serverError),
	// Get detailed lecture information
	LectureRead getLectureDetails(1: Token userToken, 2: UUID lectureId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Get list of lectures that are using the given image version
	list<LectureSummary> getLecturesByImageVersion(1: Token userToken, 2: UUID imageVersionId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Delete given lecture
	void deleteLecture(1: Token userToken, 2: UUID lectureId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Write list of permissions for given lecture
	void writeLecturePermissions(1: Token userToken, 2: UUID lectureId, 3: map<UUID, LecturePermissions> permissions)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Get list of permissions for given lecture
	map<UUID, LecturePermissions> getLecturePermissions(1: Token userToken, 2: UUID lectureId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
	// Set new owner of lecture
	void setLectureOwner(1: Token userToken, 2: UUID lectureId 3: UUID newOwnerId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInternalServerError serverError),
}

// Central master server

service MasterServer {

/*
 * Client (User's Desktop App) calls
 */
 	// Ping service
	bool ping(),

	// Old style test-account login 
	SessionData authenticate(1:string login, 2:string password)
		throws (1:TAuthorizationException failure),
	// New style test-account login 
	ClientSessionData localAccountLogin(1:string login, 2:string password)
		throws (1:TAuthorizationException failure),
	// find a user in a given organization by a search term
	list<UserInfo> findUser(1:Token sessionId, 2:string organizationId, 3:string searchTerm)
		throws (1:TAuthorizationException failure),
	// Get list of publicly available images
	list<ImagePublishData> getPublicImages(1:Token sessionId, 2:i32 page)
		throws (1:TAuthorizationException failure),

/*
 * Server (Satellite) calls
 */
 	// Verify a user by querying its meta data from the supplied token
	UserInfo getUserFromToken(1:Token token)
		throws (1:TInvalidTokenException failure),
	// Check if the server is authenticated
	bool isServerAuthenticated(1:Token serverSessionId),
	// Start authentication of server for given organization
	binary startServerAuthentication(1:string organizationId)
		throws (1: TAuthenticationException failure),
	// Reply to master server authentication challenge
	ServerSessionData serverAuthenticate(1:string organizationId, 2:binary challengeResponse)
		throws (1:TAuthenticationException failure),
	// Request upload of an image to the master server
	TransferInformation submitImage(1:Token serverSessionId, 2:ImagePublishData imageDescription, 3:list<binary> blockHashes)
		throws (1:TAuthorizationException failure, 2: TImageDataException failure2, 3: TTransferRejectedException failure3),
	// Request download of an image 
	TransferInformation getImage(2:Token serverSessionId, 1:UUID imageVersionId)
		throws (1:TAuthorizationException failure, 2: TImageDataException failure2),

	bool registerSatellite(1:string organizationId, 2:string address, 3:string modulus, 4:string exponent),

	bool updateSatelliteAddress(1:Token serverSessionId, 2:string address),

/*
 * Shared calls
 */
 	// Get list of known organizations with meta data 
	list<Organization> getOrganizations(),

	list<OperatingSystem> getOperatingSystems(),
	
	list<Virtualizer> getVirtualizers(),

}
