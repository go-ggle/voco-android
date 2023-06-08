![Group 16484](https://github.com/go-ggle/voco-android/assets/52921222/0b3c72c7-5a03-4c47-87f2-3ed3ec36339a)

## Introduction
**원하는 발음으로, 내가 말하는 듯한 외국어 더빙을 생성합니다.**  

Google Text-to-Speech를 이용하여 텍스트를 음성으로 변환한 다음, Voice Conversion 모델에 통과시켜 사용자 목소리로 변환합니다. 사용한 VC(Voice Conversion) 모델은 [StarGANv2-VC](https://starganv2-vc.github.io/)입니다. 총 **3분 분량**(2~3초 길이의 영어 80문장) 녹음만으로 사용자별 VC 모델을 생성할 수 있으며, 영어 녹음만으로 모든 언어에 대한 VC가 가능합니다.


![image](https://github.com/go-ggle/voco-android/assets/52921222/94b4f68a-d824-47fc-a505-4e9089a09c05)

## Project Structure
```
voco
├─api 
│  ├─Api.kt
│  ├─ApiRepository.kt
│  ├─EmptyResponseConverterFactory.kt
│  └─RetrofitClient.kt  
├─data  
│  ├─adapter
│  │  ├─BlockAdapter.kt
│  │  ├─ProjectAdapter.kt
│  │  ├─RecordAdapter.kt
│  │  ├─TabAdapter.kt
│  │  ├─TeamAdapter.kt
│  │  ├─VerticalItemDecoration.kt
│  │  └─HorizontalItemDecoration.kt
│  └─model
│  │  ├─AppData.kt
│  │  ├─AppDao.kt
│  │  ├─AppDatabase.kt
│  │  ├─EnumData.kt
│  │  ├─PreferenceUtil.kt
│  │  └─Dto.kt
├─dialog
│  ├─CreateProjectDialog.kt
│  └─IntervalDialog.kt
├─login
│  ├─Glob.kt
│  ├─LoginCallback.kt
│  └─SnsLogin.kt
├─service
│  ├─MediaService.kt
│  └─MyFirebaseMessagingService.kt
└─ui  
│  ├─component
│  │  ├─BlockFragment.kt
│  │  ├─ProjectFragment.kt
│  │  ├─RecordFragment.kt
│  │  ├─TabFragment.kt
│  │  ├─TeamFragment.kt
│  │  └─TeamBottomShee│.kt
│  └─pag│
│  │  ├─│plashActivity.kt
│  │  ├─│ottomNavigationActivity.kt
│  │  ├─│reateProjectActivity.kt
│  │  ├─LoginActivity.kt
│  │  ├─RecordActivity.kt
│  │  ├─SignupActivity.kt
│  │  ├─HomeFragment.kt
│  │  ├─SearchFragment.kt
│  │  └─MypageFragment.kt
```
## Library

* AudioRecord
* MediaPlayer
* ExoPlayer
* Retrofit2
* OkHttp3
* Google Firebase
* RecyclerView
* ViewPager2
* Room Database
* Preference

## How to use
#### 사용자 목소리 등록  
* 문장을 읽으면, Google Speech-to-Text를 이용하여 제대로 읽었는지 확인합니다.   
* 주어진 문장과 다른 문장을 읽었다면, 다시 읽어달라는 알림이 뜨게 됩니다.  
* 주어진 문장으로만 목소리 등록이 가능하여 타인의 목소리를 등록할 수 없습니다.  
* 총 3분 분량의 영어 문장 녹음을 진행하여 사용자의 목소리를 등록합니다.

![KakaoTalk_20230516_214326973](https://github.com/go-ggle/voco-android/assets/52921222/149a12d8-9b4a-434d-9941-e4307093332f) ![KakaoTalk_20230607_130314483](https://github.com/go-ggle/voco-android/assets/52921222/8c6ef99b-3c98-4832-b32a-26cb612eafb9)




#### 워크스페이스 생성 및 참여  
* 워크스페이스 내부에 프로젝트를 생성하여 프로젝트를 관리합니다.
* 참여코드를 이용하여 워크스페이스에 다른 사용자를 초대합니다.
* 워크스페이스를 클릭하여 현재 워크스페이스를 변경할 수 있습니다.

![KakaoTalk_20230406_170113245](https://github.com/go-ggle/voco-android/assets/52921222/e61ddbae-7de1-4326-8ea6-3b5020b43021)  ![KakaoTalk_20230405_230829298](https://github.com/go-ggle/voco-android/assets/52921222/ed616126-a86b-41bf-bc01-bfb07e2ed50d)



#### 프로젝트 생성  
* 원하는 워크스페이스를 선택하고, 프로젝트 생성 버튼을 누르면 프로젝트 언어와 제목을 선택할 수 있습니다.
* 데모에서는 12개의 언어를 지원하며, 더 많은 언어와 다양한 억양으로 확장 가능합니다.
* 제목은 프로젝트 편집 화면에서 제목을 클릭하여 수정 가능합니다. (EditTextView)

![KakaoTalk_20230607_131556519](https://github.com/go-ggle/voco-android/assets/52921222/68ba4fcc-9535-43cf-a9a7-0dbef3c23ec3) 


#### 프로젝트 편집
* 블럭 단위로 텍스트를 작성하고, 블럭별로 더빙 보이스를 선택합니다.
* 더빙 보이스는 같은 워크스페이스에 속한 사용자들의 목소리 중 선택할 수 있습니다.
* 블럭 사이의 공백 간격을 설정하여 영상과 싱크를 맞출 수 있습니다.
* 블럭이 포커스 아웃되면 비동기로 블럭별 더빙 생성 요청을 보냅니다.
* 블럭 단위, 혹은 전체 더빙을 스트리밍 및 다운로드할 수 있습니다.
* 프로젝트 내용은 자동 저장됩니다.

![KakaoTalk_20230607_135736815](https://github.com/go-ggle/voco-android/assets/52921222/ba035c84-6989-413f-ae18-0d7087594143)  ![KakaoTalk_20230523_002750554](https://github.com/go-ggle/voco-android/assets/52921222/a8b45929-6900-4ae6-a5de-e83f697dd501)


#### 로그인
* 이메일 로그인과 카카오 로그인을 지원합니다.  
* 서버로부터 받은 토큰을 Preference Util로 저장하며, 앱을 새로 실행할 때마다 토큰을 리프래시합니다.  
* [이메일 로그인] 이메일 형식이 아닌 아이디, 혹은 8자 미만의 비밀번호를 입력하면 경고알림을 띄웁니다.
* [카카오 로그인] 앱에서 직접 카카오 서버로 로그인하여 카카오 서버 토큰을 받습니다.  
이후 카카오 서버 토큰을 voco 서버에 전달하여 voco 서버의 사용자 토큰을 받습니다.    


![KakaoTalk_20230607_134715094](https://github.com/go-ggle/voco-android/assets/52921222/c810a99b-4c06-4e2d-9654-c710e8041f10)  ![KakaoTalk_20230608_162500193](https://github.com/go-ggle/voco-android/assets/52921222/b361fa3d-c6a9-4ec6-b15a-5afc9eb9f680)


#### 기타
* 프로젝트는 북마크를 설정할 수 있습니다.
* 프로젝트를 클릭하면 프로젝트 편집 화면으로 이동합니다.
* 프로젝트를 길게 클릭하여 프로젝트를 삭제할 수 있습니다.

## Developer
[정민정 - @minjungJ](https://github.com/minjungJ)

