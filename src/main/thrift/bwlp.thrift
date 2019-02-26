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
	NO_PERMISSION,
	ACCOUNT_SUSPENDED,
	ORGANIZATION_SUSPENDED,
	INVALID_CREDENTIALS,
	INVALID_ORGANIZATION,
	INVALID_KEY,
	INVALID_TOKEN,
	CHALLENGE_FAILED,
	BANNED_NETWORK
}

enum InvocationError {
	MISSING_DATA,
	INVALID_DATA,
	UNKNOWN_IMAGE,
	UNKNOWN_USER,
	UNKNOWN_LECTURE,
	INVALID_SHARE_MODE,
	INTERNAL_SERVER_ERROR
}

enum ShareMode {
	LOCAL, // Managed by local user, do not upload to master server
	PUBLISH, // Managed by local user, upload new versions to master server
	DOWNLOAD, // Managed by remote user, automatically download new versions
	FROZEN // Managed by remote user, but do not download any new versions
}

enum NetDirection {
	IN,
	OUT
}

enum Role {
	STUDENT,
	TUTOR
}

enum TransferState {
	IDLE,
	WORKING,
	FINISHED,
	ERROR
}

enum DateParamError {
	TOO_LOW,
	TOO_HIGH,
	NEGATIVE_RANGE
}

enum NetShareAuth {
	LOGIN_USER,
	OTHER_USER
}

enum SscMode {
	OFF,
	ON,
	AUTO,
	USER,
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

struct WhoamiInfo {
	1: UserInfo user,
	2: bool isSuperUser,
	3: bool canListImages,
}

struct Organization {
	1: string organizationId,
	2: string displayName,
	3: string ecpUrl,
	4: list<string> suffixList,
}

struct Satellite {
	1: list<string> addressList,
	2: string displayName,
	3: binary certSha256
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
	3: list<Satellite> satellites,
	4: UserInfo userInfo,
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
	5: i32 maxMemMb,
	6: i32 maxCores,
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
	8: optional list<string> addTags,
	9: optional list<string> remTags,
}

struct ImageVersionWrite {
	2: bool isRestricted,
}

struct ImageSummaryRead {
	1: UUID imageBaseId,
	3: UUID latestVersionId,
	4: string imageName,
	5: i32 osId,
	6: string virtId,
	7: UnixTimestamp createTime,
	8: UnixTimestamp updateTime,
	20: UnixTimestamp uploadTime, // Time when the latest version was uploaded
	9: UnixTimestamp expireTime,
	10: UUID ownerId,
	11: UUID uploaderId, // Uploader of the latest version
	12: ShareMode shareMode,
	13: i64 fileSize, // Size of the latest version
	14: bool isRestricted,
	15: bool isValid,
	16: bool isProcessed,
	17: bool isTemplate,
	18: ImagePermissions defaultPermissions,
	19: optional ImagePermissions userPermissions,
	21: optional i64 fileSizeSum,
	22: optional i32 versionCount,
}

struct ImageVersionDetails {
	1: UUID versionId,
	2: UnixTimestamp createTime,
	3: UnixTimestamp expireTime,
	4: i64 fileSize,
	5: UUID uploaderId,
	7: bool isRestricted,
	8: bool isValid,
	9: bool isProcessed,
	10: list<string> software,
}

struct ImageDetailsRead {
	1: UUID imageBaseId,
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

struct ImagePublishData {
	1: UUID imageBaseId,
	2: UUID imageVersionId,
	3: string imageName,
	4: string description,
	5: UnixTimestamp createTime,
	6: UserInfo uploader,
	7: i64 fileSize,
	8: list<string> software,
	9: list<string> tags,
	10: i32 osId,
	11: string virtId,
	12: bool isTemplate,
	13: UserInfo owner,
	14: binary machineDescription,
}

struct NetRule {
	2: NetDirection direction,
	3: string host,
	4: i32 port,
}

struct NetShare {
	4: NetShareAuth auth,
	1: string path,
	5: optional string displayname,
	6: optional string mountpoint,
	2: optional string username,
	3: optional string password,
	7: optional i32 shareId,
}

struct LdapFilter {
	1: string attribute,
	2: string value,
	3: optional i32 filterId,
	4: optional string title,
}

struct PresetRunScript {
	1: i32 scriptId,
	2: string displayname,
	3: list<i32> osIds,
}

struct PresetNetRule {
	1: i32 ruleId,
	2: string displayName,
	3: list<NetRule> netRules,
}

struct PredefinedData {
	1: list<NetShare> netShares,
	2: list<LdapFilter> ldapFilter,
	3: list<PresetRunScript> runScripts,
	4: list<PresetNetRule> networkExceptions,
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
	12: optional list<NetRule> networkExceptions, // Atomically replace rules
	13: bool isExam,
	14: bool hasInternetAccess,
	15: LecturePermissions defaultPermissions,
	11: optional list<string> addAllowedUsers, // add allowed to see/select image in vmchooser. These are local accounts, not bwIDM/Master
	16: optional list<string> remAllowedUsers, // users to remove from that list
	17: list<i32> locationIds,
	18: bool limitToLocations,
	19: bool limitToAllowedUsers,
	20: bool hasUsbAccess,
	21: optional list<NetShare> networkShares,
	22: optional list<LdapFilter> ldapFilters,
	23: optional list<i32> presetScriptIds,
	24: optional list<i32> presetNetworkExceptionIds,
}

struct LectureSummary {
	1: UUID lectureId,
	2: string lectureName,
	3: UUID imageVersionId,
	4: UUID imageBaseId,
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
	17: bool hasUsbAccess,
}

struct LectureRead {
	1: UUID lectureId,
	2: string lectureName,
	3: string description,
	23: string imageVersionId,
	24: string imageBaseId,
	5: bool autoUpdate,
	6: bool isEnabled,
	7: UnixTimestamp startTime,
	8: UnixTimestamp endTime,
	9: UnixTimestamp lastUsed,
	10: i32 useCount,
	20: UnixTimestamp createTime,
	21: UnixTimestamp updateTime,
	11: UUID ownerId,
	12: UUID updaterId,
	13: string runscript,
	14: list<string> nics,
	15: list<string> allowedUsers, // allowed to see/select image in vmchooser. These are local accounts, not bwIDM/Master
	16: list<NetRule> networkExceptions,
	17: bool isExam,
	18: bool hasInternetAccess,
	19: LecturePermissions defaultPermissions,
	22: optional LecturePermissions userPermissions,
	25: list<i32> locationIds,
	26: bool limitToLocations,
	27: bool limitToAllowedUsers,
	28: bool hasUsbAccess,
	29: optional list<NetShare> networkShares,
	30: optional list<LdapFilter> ldapFilters,
	31: optional list<i32> presetScriptIds,
	32: optional list<i32> presetNetworkShares,
	33: optional list<i32> presetLdapFilters,
	34: optional list<i32> presetNetworkExceptionIds,
}

struct MasterTag {
	1: string tag,
	2: UnixTimestamp createTime,
}

struct MasterSoftware {
	1: string software,
	2: bool isRestricted,
	3: UnixTimestamp createTime,
}

struct TransferInformation {
	1: Token token,
	2: i32 plainPort,
	3: i32 sslPort,
	4: optional list<binary> blockHashes, // Only if transfer is a download (and list is known)
	5: optional binary machineDescription, // Only if transfer is a download
}

// Used to tell status of an upload. The blockStatus is one byte per 16MB block,
// 0 = complete, 1 = missing, 2 = uploading, 3 = queued for copying, 4 = copying, 5 = hashing
struct TransferStatus {
	1: binary blockStatus,
	2: TransferState state,
}

struct UploadOptions {
	1: bool serverSideCopying,
}

struct SatelliteConfig {
	// Get number of items returned per page (for calls that have a page parameter)
	1: i32 pageSize,
	// Which permissions to pre-select when creating a new image
	2: ImagePermissions defaultImagePermissions,
	// Which permissions to pre-select when creating a new lecture
	3: LecturePermissions defaultLecturePermissions,
	// Maximum number of days the expiration date of an image may be set in the future
	4: i32 maxImageValidityDays,
	// Maximum number of days the expiration date of a lecture may be set in the future
	5: i32 maxLectureValidityDays,
	// Maximum number of concurrent transfers (individual uploads/downloads)
	6: optional i32 maxTransfers,
	// Maximum number of connections per transfer
	7: optional i32 maxConnectionsPerTransfer,
	// Maximum number of locations per lecture
	8: optional i32 maxLocationsPerLecture,
	// Whether users connecting to the sat for the first time are allowed to login
	9: optional bool allowLoginByDefault,
	// ServerSide Copy on, off, auto or controlled by user
	10: optional SscMode serverSideCopy,
}

struct SatelliteStatus {
	1: i64 availableStorageBytes,
	2: UnixTimestamp serverTime,
}

// Settings a user can change on a satellite server
struct SatelliteUserConfig {
	1: bool emailNotifications,
}

// Location of a Lecture
struct Location {
	1: i32 locationId,
	2: string locationName,
	3: i32 parentLocationId,
}

// ############ EXCEPTION ######################

exception TTransferRejectedException {
	1: string message,
}

exception TAuthorizationException {
	1: AuthorizationError number,
	2: string message
}

exception TInvalidTokenException {
}

exception TNotFoundException {
	1: string message
}

exception TInvalidDateParam {
	1: DateParamError number,
	2: string message,
}

exception TInvocationException {
	1: InvocationError number,
	2: string message
}

// #############################################

service SatelliteServer {
	// Get server (thrift interface) version
	int getVersion(1: int clientVersion),

	// Get server features. Kinda superseding getVersion, as it's reasier to handle minor updates
	// This returns a space separated list of keywords which represent certain features
	string getSupportedFeatures(),

	// Get configuration parameters of this satellite server
	SatelliteConfig getConfiguration(),

	/*
	 * File transfer related
	 */

	// Client wants to upload an image
	TransferInformation requestImageVersionUpload(1: Token userToken, 2: UUID imageBaseId, 3: i64 fileSize, 4: list<binary> blockHashes, 5: binary machineDescription)
		throws (1:TTransferRejectedException rejection, 2:TAuthorizationException authError, 3:TInvocationException ffff, 4:TNotFoundException sdf),

	// Client updates block hashes of an upload
	void updateBlockHashes(1: Token uploadToken, 2: list<binary> blockHashes, 3: Token userToken)
		throws (1:TInvalidTokenException ex1),

	// Change settings for a specific upload
	UploadOptions setUploadOptions(1: Token userToken, 2: Token uploadToken, 3: UploadOptions options)
		throws (1:TAuthorizationException frootloops, 2:TInvalidTokenException imcyborgbutthatsok),

	// Client cancels an upload
	void cancelUpload(1: Token uploadToken)
		throws (1:TInvalidTokenException ex1),

	// Client queries server-side status of an upload
	TransferStatus queryUploadStatus(1: Token uploadToken)
		throws (1:TInvalidTokenException ex1),

	// Client wants to download an image
	TransferInformation requestDownload(1: Token userToken, 2: UUID imageVersionId)
		throws (1:TTransferRejectedException rejection, 2:TAuthorizationException authError, 3:TInvocationException ffff, 4:TNotFoundException sdf),

	// Client cancels a download
	void cancelDownload(1: string downloadToken)
		throws (1:TInvalidTokenException ex1),

	/*
	 * Auth/Session
	 */

	// Authentication check (deprecated, superseded by whoami)
	void isAuthenticated(1: Token userToken)
		throws (1:TAuthorizationException authError, 2:TInvocationException serverError),

	// Query own user information (for validation or session resume)
	WhoamiInfo whoami(1: Token userToken)
		throws (1:TAuthorizationException authError, 2:TInvocationException serverError),

	// Logout - make server forget given token
	void invalidateSession(1: Token userToken)
		throws (1:TInvalidTokenException ex),

	// find a user in a given organization by a search term
	list<UserInfo> getUserList(1:Token userToken, 2:i32 page)
		throws (1:TAuthorizationException failure, 2:TInvocationException serverError),

	// Get user configurable options
	SatelliteUserConfig getUserConfig(1:Token userToken)
		throws (1:TAuthorizationException failure, 2:TInvocationException serverError),

	// Set user configurable options
	void setUserConfig(1:Token userToken, 2:SatelliteUserConfig config)
		throws (1:TAuthorizationException failure, 2:TInvocationException serverError),

	/*
	 * Misc
	 */
 
	list<OperatingSystem> getOperatingSystems(),
	list<Virtualizer> getVirtualizers(),
	list<Organization> getAllOrganizations(),
	list<Location> getLocations(),

	SatelliteStatus getStatus(),

	/*
	 * Image related
	 */

	// Get image list. tagSearch can be null, which disables this type of filtering and returns all
	list<ImageSummaryRead> getImageList(1: Token userToken, 2: list<string> tagSearch, 3: i32 page)
		throws (1:TAuthorizationException authError, 2:TInvocationException serverError),

	// Query detailed information about an image
	ImageDetailsRead getImageDetails(1: Token userToken, 2: UUID imageBaseId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Create a new image; the image will have no versions, so the user needs to upload one and set meta data later on
	UUID createImage(1: Token userToken, 2: string imageName)
		throws (1:TAuthorizationException authError, 2:TInvocationException error),

	// Update given image's base meta data
	void updateImageBase(1: Token userToken, 2: UUID imageBaseId 3: ImageBaseWrite image)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException imgError),

	// Update a certain image version's meta data
	void updateImageVersion(1: Token userToken, 2: UUID imageVersionId 3: ImageVersionWrite image)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException imgError),

	// Delete given image version. If the version is currently in use by a lecture, it will not be

	// deleted and false is returned
	void deleteImageVersion(1: Token userToken, 2: UUID imageVersionId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Delete image and all its versions
	void deleteImageBase(1:Token userToken, 2:UUID imageBaseId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Write list of permissions for given image
	void writeImagePermissions(1: Token userToken, 2: UUID imageBaseId, 3: map<UUID, ImagePermissions> permissions)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Get all user-permissions for given image
	map<UUID, ImagePermissions> getImagePermissions(1: Token userToken, 2: UUID imageBaseId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Set new owner of image
	void setImageOwner(1: Token userToken, 2: UUID imageBaseId 3: UUID newOwnerId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Set image version valid and change expire date (super user action)
	void setImageVersionExpiry(1: Token userToken, 2: UUID imageBaseId 3: UnixTimestamp expireTime)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError, 4:TInvalidDateParam dateError),

	// Get image's VM metadata for the given version
	binary getImageVersionVirtConfig(1: Token userToken, 2: UUID imageVersionId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Set image's VM metadata for the given version
	void setImageVersionVirtConfig(1: Token userToken, 2: UUID imageVersionId, 3: binary meta)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Client asks server to replicate an image from the master server
	UUID requestImageReplication(1:Token userToken, 2: UUID imageVersionId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Client asks the satellite to publish given image to master server
	UUID publishImageVersion(1:Token userToken, 2: UUID imageVersionId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError, 4:TTransferRejectedException tre),

	/*
	 * Lecture related
	 */

	// Create new lecture
	UUID createLecture(1: Token userToken, 2: LectureWrite lecture)
		throws (1:TAuthorizationException authError, 2:TInvocationException serverError, 3:TInvalidDateParam dateError, 4:TNotFoundException notFound),

	// Update existing lecture
	void updateLecture(1: Token userToken, 2: UUID lectureId, 3: LectureWrite lecture)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError, 4:TInvalidDateParam dateError),

	// Get list of all lectures
	list<LectureSummary> getLectureList(1: Token userToken, 2: i32 page)
		throws (1:TAuthorizationException authError, 2:TInvocationException serverError),

	// Get detailed lecture information
	LectureRead getLectureDetails(1: Token userToken, 2: UUID lectureId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Delete given lecture
	void deleteLecture(1: Token userToken, 2: UUID lectureId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Write list of permissions for given lecture
	void writeLecturePermissions(1: Token userToken, 2: UUID lectureId, 3: map<UUID, LecturePermissions> permissions)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Get list of permissions for given lecture
	map<UUID, LecturePermissions> getLecturePermissions(1: Token userToken, 2: UUID lectureId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Set new owner of lecture
	void setLectureOwner(1: Token userToken, 2: UUID lectureId 3: UUID newOwnerId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Get predefined stuff
	PredefinedData getPredefinedData(1: Token userToken)
		throws (1:TAuthorizationException authError, 2:TInvocationException serverError),

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
		throws (1:TAuthorizationException failure, 2:TInvocationException error),

	// New style test-account login
	ClientSessionData localAccountLogin(1:string login, 2:string password)
		throws (1:TAuthorizationException failure, 2:TInvocationException error),

	// Client tells us which satellite it is using
	void setUsedSatellite(1:Token sessionId, 2:string satelliteName),

	// find a user in a given organization by a search term
	list<UserInfo> findUser(1:Token sessionId, 2:string organizationId, 3:string searchTerm)
		throws (1:TAuthorizationException failure, 2:TInvocationException error),

	// Get list of publicly available images
	list<ImageSummaryRead> getPublicImages(1:Token sessionId, 2:i32 page)
		throws (1:TAuthorizationException failure, 2:TInvocationException error),

	// Query detailed information about an image
	ImageDetailsRead getImageDetails(1: Token sessionId, 2: UUID imageBaseId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Get user by id
	UserInfo getUser(1: Token userToken, 2: UUID userId)
		throws (1:TAuthorizationException authError, 2:TNotFoundException notFound, 3:TInvocationException serverError),

	// Logout
	void invalidateSession(1: Token sessionId)
		throws (1:TInvalidTokenException ex),

	/*
	 * Server (Satellite) calls
	 */

	// Verify a user by querying its meta data from the supplied token
	UserInfo getUserFromToken(1:Token token)
		throws (1:TInvalidTokenException failure),

	// Check if the server is authenticated
	bool isServerAuthenticated(1:Token serverSessionId),

	// Start authentication of server for given organization
	binary startServerAuthentication(1:i32 satelliteId)
		throws (1: TAuthorizationException failure, 2:TInvocationException error),

	// Reply to master server authentication challenge
	ServerSessionData serverAuthenticate(1:i32 satelliteId, 2:binary challengeResponse)
		throws (1:TAuthorizationException failure, 2:TInvocationException errr),

	// Get image information (prior to download)
	ImagePublishData getImageData(1:Token serverSessionId, 2:UUID imageVersionId)
		throws (1:TAuthorizationException failure, 2: TInvocationException failure2, 3:TNotFoundException f3),

	// Request upload of an image to the master server
	TransferInformation submitImage(1:Token userToken, 2:ImagePublishData imageDescription, 3:list<binary> blockHashes)
		throws (1:TAuthorizationException failure, 2: TInvocationException failure2, 3: TTransferRejectedException failure3),

	i32 registerSatellite(6:Token userToken, 5:string displayName, 2:list<string> addresses, 3:string modulus, 4:string exponent, 1:binary certsha256)
		throws (1:TInvocationException error),

	bool updateSatellite(1:Token serverSessionId, 2:string displayName, 3:list<string> addresses)
		throws (1:TAuthorizationException failure, 2:TInvocationException error),

	/*
	 * Shared calls
	 */

	// Request download of an image (session id can be a client or a server session)
	TransferInformation downloadImage(2:Token sessionId, 1:UUID imageVersionId)
		throws (1:TAuthorizationException failure, 2:TInvocationException failure2, 3:TNotFoundException f3),

	// Get list of known organizations with meta data
	list<Organization> getOrganizations()
		throws (1:TInvocationException serverError),

	// List of known/defined operating systems
	list<OperatingSystem> getOperatingSystems()
		throws (1:TInvocationException serverError),

	// List of known/defined virtualizers
	list<Virtualizer> getVirtualizers()
		throws (1:TInvocationException serverError),

	// List of "official" tags, starting from specific date
	list<MasterTag> getTags(1:UnixTimestamp startDate)
		throws (1:TInvocationException serverError),

	// List of "official" software, starting from specific date
	list<MasterSoftware> getSoftware(1:UnixTimestamp startDate)
		throws (1:TInvocationException serverError),

	// Client (maybe later server too) queries server-side status of an upload
	TransferStatus queryUploadStatus(1: Token uploadToken)
		throws (1:TInvalidTokenException ex1),

}
