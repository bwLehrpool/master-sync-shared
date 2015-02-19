/**
 * Define some namespace/package name for our stuff
 */
namespace java org.openslx.sat.thrift.iface
 
typedef i64 int

struct User{
	1: string userName,
	2: string password,
	3: string path,
}

struct Image{
	1: string id,
	2: string version,
	3: string imageName,
	4: string licenseRestriction,
	5: string osName,
	6: string lectureName,
	7: string updateTime,
	8: string userData,
	9: string isTemplate,
   10: string description,
   11: string imageSize,
}
struct Lecture{
	1: string id,
	2: string lecturename,
	3: string isActive,
	4: string starttime,
	5: string endtime,
	6: string lastused,
	7: string desc,
	8: string imagename,
	9: string username,
}
struct Person{
	1: string userID,
	2: string Nachname,
	3: string Vorname,
	4: string mail,
	5: bool image_read
	6: bool image_write
	7: bool image_admin
	8: bool image_link
	9: bool lecture_read
	10: bool lecture_write
	11: bool lecture_admin
}
service Server{
	int getVersion(),
	User getFtpUser(1: string token),
	bool authenticated(1: string token),
	bool setSessionInvalid(1: string token),
	int DeleteFtpUser(1: string user, 2: string token),
    string getPathOfImage(1: string image_id, 2: string version, 3: string token),
    string setInstitution(1: string university, 2:string token),
    bool writeVLdata(1: string imagename, 2: string desc, 8: string Tel, 9: string Fak, 10: bool license, 11: bool internet, 12: int ram, 13: int cpu, 14: string imagePath, 15: bool isTemplate, 16: i64 filesize, 17: int shareMode, 18: string os, 19: string uid, 20:string token, 21: string userID),
    list<Image> getImageListPermissionWrite(1: string userID, 2: string token),
    list<Image> getImageListPermissionRead(1: string userID, 2: string token),
    list<Image> getImageListPermissionLink(1: string userID, 2: string token),
    list<Image> getImageListPermissionAdmin(1: string userID, 2: string token),
    list<Image> getImageListAllTemplates(1: string token),
    list<Image> getImageList(1: string userID, 2: string token),
    list<Lecture> getLectureList(1: string token),
    list<Lecture> getLectureListPermissionRead(1: string token),
    list<Lecture> getLectureListPermissionWrite(1: string token),
    list<Lecture> getLectureListPermissionAdmin(1: string token),
    list<string> getAllOS(1: string token),
    list<string> getAllUniversities(1: string token),
    map<string,string> getPersonData(1: string Vorname, 2: string Nachname, 3: string token),
    map<string,string> getItemOwner(1: string itemID, 2: string token),
    void setPerson(1: string userID, 2: string token, 3: string institution),
    bool writeLecturedata(1: string name, 2: string shortdesc, 3: string desc, 4: string startDate, 5: string endDate, 6: bool isActive, 7: string imagename, 8: string token, 13: string Tel, 14: string Fak, 16: string lectureID, 17: string university),
    bool startFileCopy(1: string file, 2:string token),
	map<string,string> getImageData(1: string imageid, 2: string imageversion, 3: string token),
	map<string,string> getLectureData(1: string lectureid, 2: string token),
	bool updateImageData(1: string name, 2: string newName, 3: string desc, 4: string image_path, 5: bool license, 6: bool internet, 7: int ram, 8: int cpu, 9: string id, 10: string version, 11: bool isTemplate, 12: i64 filesize, 13: int shareMode, 14: string os, 15: string token),
	bool deleteImageData(1: string id, 2: string version, 3: string token),
	bool updateLecturedata(1: string name, 2: string newName, 3: string shortdesc, 4: string desc, 5: string startDate, 6: string endDate, 7: bool isActive, 8: string imageid, 9: string imageversion, 10: string token, 15: string Tel, 16: string Fak, 17: string id, 18: string university),
	bool deleteImageServer(1: string id, 2: string version, 3: string token),
	bool deleteImageByPath(1: string image_path),
	bool connectedToLecture(1: string id, 2: string version, 3: string token),
	bool deleteLecture(1: string id, 2: string token, 3: string university),
	bool checkUser(1: string username, 2: string token),
	bool createUser(1: string token, 2: string university),
	bool writeImageRights(1: string imagename, 2: string token, 3: string role, 4: string university, 5: string userID),
	bool writeAdditionalImageRights(1: string imageName, 2: string userID, 3: bool isRead, 4: bool isWrite, 5: bool isLinkAllowed, 6: bool isAdmin, 7: string token),
	bool writeLectureRights(1: string lectureID, 2: string role, 3: string token, 4: string university, 5: string userID),
	bool writeAdditionalLectureRights(1: string lectureName, 2: string userID, 3: bool isRead, 4: bool isWrite, 5: bool isAdmin, 6: string token),
	list<Person>getAllOtherSatelliteUsers(1: list<string> userID, 2: string token),
	list<Person>getPermissionForUserAndImage(1: string token, 2: string imageID, 3: string userID),
	list<string>getAdditionalImageContacts(1: string imageID, 2: string token),
	list<Person>getPermissionForUserAndLecture(1: string token, 2: string lectureID, 3: string userID),
	void deleteAllAdditionalImagePermissions(1: string imageID, 2:string token, 3: string userID),
	void deleteAllAdditionalLecturePermissions(1: string lectureID, 2: string token, 3: string userID),
	string getOsNameForGuestOs(1: string guestOS, 2: string token),
	bool userIsImageAdmin(1: string imageID, 2: string token, 3: string userID),
	bool userIsLectureAdmin(1: string userID, 2: string lectureID, 3: string token),
	string createRandomUUID(1: string token),
	string getInstitutionByID(1: string institutionID)

}
