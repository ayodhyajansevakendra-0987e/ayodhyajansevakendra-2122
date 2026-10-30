package in.nooralam.janseva

import android.os.Bundle
import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import java.util.concurrent.TimeUnit
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val Navy = Color(0xFF123B70)
private val Blue = Color(0xFF1769AA)
private val services = listOf(
    Service("pan_apply","PAN Card Apply","पैन कार्ड आवेदन","Identity"),
    Service("pan_correction","PAN Card Correction","पैन कार्ड सुधार","Identity"),
    Service("aadhaar_help","Aadhaar Assistance","आधार सहायता (केवल अधिकृत सेवाएँ)","Identity"),
    Service("voter_apply","Voter ID Apply","वोटर आईडी आवेदन","Identity"),
    Service("voter_correction","Voter ID Correction","वोटर आईडी सुधार","Identity"),
    Service("income","Income Certificate","आय प्रमाण पत्र","Certificate"),
    Service("caste","Caste Certificate","जाति प्रमाण पत्र","Certificate"),
    Service("domicile","Domicile Certificate","निवास प्रमाण पत्र","Certificate"),
    Service("birth","Birth Certificate","जन्म प्रमाण पत्र","Certificate"),
    Service("death","Death Certificate","मृत्यु प्रमाण पत्र","Certificate"),
    Service("online_form","Online Form Filling","ऑनलाइन फॉर्म भरना","Forms"),
    Service("jobs","Government Job Forms","सरकारी नौकरी फॉर्म","Forms"),
    Service("scholarship","Scholarship Forms","छात्रवृत्ति फॉर्म","Forms"),
    Service("electricity","Electricity Bill Payment","बिजली बिल भुगतान","Payments"),
    Service("recharge","Mobile / DTH Recharge","मोबाइल / डीटीएच रिचार्ज","Payments"),
    Service("print","Printout & Photocopy","प्रिंटआउट और फोटोकॉपी","Shop"),
    Service("passport","Passport Assistance","पासपोर्ट आवेदन सहायता","Assistance")
)
data class Service(val id:String,val en:String,val hi:String,val category:String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent { NoorApp() }
    }
}

@Composable
fun NoorApp() {
    var hindi by remember { mutableStateOf(true) }
    val nav = rememberNavController()
    MaterialTheme(colorScheme = lightColorScheme(primary = Navy, secondary = Blue)) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    listOf("home" to "Home", "requests" to "My Requests", "profile" to "Profile").forEach { (route,label) ->
                        NavigationBarItem(
                            selected = nav.currentBackStackEntryAsState().value?.destination?.route == route,
                            onClick = { nav.navigate(route) { launchSingleTop = true } },
                            icon = { Icon(if(route=="home") Icons.Default.Home else if(route=="requests") Icons.Default.ReceiptLong else Icons.Default.Person, null) },
                            label = { Text(if(hindi) when(route) {"home"->"होम";"requests"->"मेरे आवेदन";else->"प्रोफाइल"} else label) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(navController=nav,startDestination="home",modifier=Modifier.padding(padding)) {
                composable("home") { HomeScreen(hindi, { hindi = !hindi }, { nav.navigate("apply/${it.id}") }) }
                composable("requests") { RequestsScreen(hindi) }
                composable("profile") { ProfileScreen(hindi) }
                composable("apply/{id}") { entry ->
                    val id = entry.arguments?.getString("id") ?: ""
                    ApplyScreen(services.firstOrNull { it.id == id }, hindi)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(hindi:Boolean, toggle:()->Unit, open:(Service)->Unit) {
    var query by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(top=16.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
                Column {
                    Text("NOOR ALAM", color=Navy, style=MaterialTheme.typography.titleLarge, fontWeight=FontWeight.Bold)
                    Text("JAN SEVA KENDRA", color=Blue, style=MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick=toggle) { Text(if(hindi) "English" else "हिंदी") }
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Navy), shape=RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text(if(hindi) "नमस्कार! स्वागत है" else "Welcome!", color=Color.White, style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
                    Text(if(hindi) "सभी ऑनलाइन सेवाएँ एक ही जगह" else "Online services, all in one place", color=Color.White)
                    Spacer(Modifier.height(10.dp))
                    Text("Faizabad, Ayodhya • 224001", color=Color.White)
                }
            }
        }
        item {
            OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text(if(hindi) "सेवा खोजें..." else "Search services...")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true)
        }
        item { Text(if(hindi) "हमारी सेवाएँ" else "Our Services", style=MaterialTheme.typography.titleLarge, fontWeight=FontWeight.Bold) }
        val filtered=services.filter { it.en.contains(query,true) || it.hi.contains(query,true) }
        items(filtered) { service ->
            Card(Modifier.fillMaxWidth().clickable { open(service) }, elevation=CardDefaults.cardElevation(2.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment=Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment,color=Blue,modifier=Modifier.size(30.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if(hindi) service.hi else service.en,fontWeight=FontWeight.SemiBold)
                        Text(service.category,style=MaterialTheme.typography.bodySmall,color=Color.Gray)
                    }
                    Icon(Icons.Default.ChevronRight,null)
                }
            }
        }
        item {
            Card(colors=CardDefaults.cardColors(containerColor=Color(0xFFEAF3FF))) {
                Column(Modifier.padding(16.dp)) {
                    Text(if(hindi) "संपर्क करें" else "Contact us",fontWeight=FontWeight.Bold)
                    Text("Noor Alam Ansari")
                    Text("8005285706  •  9335292993")
                    Text("WhatsApp support: wa.me/918005285706",color=Blue)
                }
            }
        }
        item { Text("Service requests are assistance workflows. Government services are subject to official eligibility, portals and authorization.",style=MaterialTheme.typography.bodySmall,color=Color.Gray) }
    }
}

@Composable
fun ApplyScreen(service:Service?, hindi:Boolean) {
    val auth=FirebaseAuth.getInstance()
    val db=FirebaseFirestore.getInstance()
    val storage=FirebaseStorage.getInstance()
    val context= LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedDoc by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope= rememberCoroutineScope()
    val picker= rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedDoc=uri
        if(uri!=null) {
            try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            catch (_:Exception) { }
        }
    }
    Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(if(hindi) "सेवा अनुरोध" else "Service request",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        Text(if(hindi) service?.hi ?: "" else service?.en ?: "")
        OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text(if(hindi) "पूरा नाम" else "Full name")})
        OutlinedTextField(phone,{phone=it},Modifier.fillMaxWidth(),label={Text(if(hindi) "मोबाइल नंबर" else "Mobile number")})
        OutlinedTextField(details,{details=it},Modifier.fillMaxWidth(),minLines=4,label={Text(if(hindi) "आवश्यक जानकारी" else "Request details")})
        OutlinedButton(onClick={picker.launch(arrayOf("application/pdf","image/jpeg","image/png"))},modifier=Modifier.fillMaxWidth()) {
            Icon(Icons.Default.AttachFile,null); Spacer(Modifier.width(8.dp))
            Text(if(selectedDoc==null) { if(hindi) "दस्तावेज़ चुनें (PDF/JPG/PNG)" else "Choose document (PDF/JPG/PNG)" } else selectedDoc?.lastPathSegment ?: "Document selected")
        }
        Text("Do not upload Aadhaar OTP, passwords, PINs or unnecessary sensitive data.",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
        Button(onClick={
            if(auth.currentUser==null) { message=if(hindi) "पहले प्रोफाइल में मोबाइल OTP से लॉगिन करें।" else "Please login with mobile OTP from Profile first."; return@Button }
            if(name.isBlank() || phone.length<10 || service==null) { message=if(hindi) "कृपया नाम और सही मोबाइल नंबर भरें" else "Enter your name and a valid mobile number"; return@Button }
            scope.launch {
                busy=true
                try {
                    val uid=auth.currentUser?.uid ?: throw IllegalStateException("Please login first")
                    val paths= mutableListOf<String>()
                    selectedDoc?.let { uri ->
                        val mime=context.contentResolver.getType(uri) ?: "application/octet-stream"
                        require(mime in listOf("application/pdf","image/jpeg","image/png")) { "Only PDF, JPG and PNG are accepted" }
                        val path="customer-documents/$uid/${System.currentTimeMillis()}_${service.id}"
                        storage.reference.child(path).putFile(uri).await()
                        paths.add(path)
                    }
                    val ref=db.collection("applications").add(hashMapOf(
                        "customerId" to uid, "customerName" to name, "phone" to phone,
                        "serviceId" to service.id, "serviceName" to service.en,
                        "details" to details, "documentPaths" to paths,
                        "status" to "submitted", "createdAt" to com.google.firebase.Timestamp.now(),
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )).await()
                    message=if(hindi) "आवेदन जमा हो गया। आवेदन आईडी: ${ref.id}" else "Request submitted. Application ID: ${ref.id}"
                    name=""; phone=""; details=""; selectedDoc=null
                } catch(e:Exception) { message="Submit failed: ${e.localizedMessage ?: "Check Firebase setup"}" }
                busy=false
            }
        },enabled=!busy,modifier=Modifier.fillMaxWidth()) { Text(if(busy) "Uploading / submitting…" else if(hindi) "अनुरोध जमा करें" else "Submit request") }
        if(message.isNotBlank()) Text(message)
    }
}

@Composable
fun RequestsScreen(hindi:Boolean) {
    var rows by remember { mutableStateOf<List<Map<String,Any>>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    val user=FirebaseAuth.getInstance().currentUser
    DisposableEffect(user?.uid) {
        if(user==null) { rows= emptyList(); onDispose { } }
        else {
            val registration=FirebaseFirestore.getInstance().collection("applications")
                .whereEqualTo("customerId",user.uid).addSnapshotListener { snap,e ->
                    if(e!=null) error=e.localizedMessage ?: "Unable to load"
                    else rows=snap?.documents?.mapNotNull { doc -> doc.data?.plus("applicationId" to doc.id) } ?: emptyList()
                }
            onDispose { registration.remove() }
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text(if(hindi) "मेरे आवेदन" else "My requests",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
        if(error.isNotBlank()) Text(error,color=Color.Red)
        if(user==null) Text(if(hindi) "आवेदन देखने के लिए प्रोफाइल में OTP लॉगिन करें।" else "Login with OTP from Profile to view requests.")
        else if(rows.isEmpty()) Text(if(hindi) "अभी कोई आवेदन नहीं है।" else "No applications yet.")
        rows.forEach { row ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    Text(row["serviceName"]?.toString() ?: "Service",fontWeight=FontWeight.Bold)
                    Text("ID: ${row["applicationId"] ?: ""}",style=MaterialTheme.typography.bodySmall)
                    Text("Status: ${row["status"] ?: "submitted"}")
                    Text(row["details"]?.toString() ?: "",style=MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}


@Composable
fun PhoneOtpScreen(hindi:Boolean, onDone:()->Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var phone by remember { mutableStateOf("+91") }
    var code by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var resendToken by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    fun saveCustomerProfile(uid:String, number:String) {
        db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                db.collection("users").document(uid).set(
                    mapOf("phone" to number, "role" to "customer", "preferredLanguage" to if(hindi) "hi" else "en")
                ).addOnSuccessListener { status = if(hindi) "लॉगिन सफल" else "Login successful"; onDone() }
                 .addOnFailureListener { status = it.localizedMessage ?: "Profile setup failed" }
            } else { onDone() }
        }.addOnFailureListener { status = it.localizedMessage ?: "Profile lookup failed" }
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(if(hindi) "मोबाइल OTP लॉगिन" else "Mobile OTP Login", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
        if(verificationId == null) {
            OutlinedTextField(phone,{phone=it},Modifier.fillMaxWidth(),label={Text(if(hindi) "मोबाइल नंबर (+91 सहित)" else "Mobile number (with +91)")},singleLine=true)
            Button(enabled=!busy && phone.startsWith("+") && phone.length>=12,onClick={
                if(activity==null) { status="Activity unavailable"; return@Button }
                busy=true
                val options=PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone.trim())
                    .setTimeout(60L,TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object:PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                        override fun onVerificationCompleted(credential:PhoneAuthCredential) {
                            auth.signInWithCredential(credential).addOnSuccessListener { result ->
                                val uid=result.user?.uid
                                if(uid!=null) saveCustomerProfile(uid,result.user?.phoneNumber ?: phone)
                            }.addOnFailureListener { status=it.localizedMessage ?: "Sign-in failed" }
                            busy=false
                        }
                        override fun onVerificationFailed(e:com.google.firebase.FirebaseException) {
                            status=e.localizedMessage ?: "OTP verification failed"; busy=false
                        }
                        override fun onCodeSent(id:String,token:PhoneAuthProvider.ForceResendingToken) {
                            verificationId=id;resendToken=token;status=if(hindi) "OTP भेज दिया गया है" else "OTP sent";busy=false
                        }
                    }).build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            },modifier=Modifier.fillMaxWidth()) { Text(if(busy) "..." else if(hindi) "OTP भेजें" else "Send OTP") }
        } else {
            OutlinedTextField(code,{code=it},Modifier.fillMaxWidth(),label={Text(if(hindi) "OTP दर्ज करें" else "Enter OTP")},singleLine=true)
            Button(enabled=!busy && code.length>=6,onClick={
                busy=true
                val credential=PhoneAuthProvider.getCredential(verificationId!!,code.trim())
                auth.signInWithCredential(credential).addOnSuccessListener { result ->
                    val u=result.user
                    if(u!=null) saveCustomerProfile(u.uid,u.phoneNumber ?: phone)
                    busy=false
                }.addOnFailureListener { status=it.localizedMessage ?: "Invalid OTP";busy=false }
            },modifier=Modifier.fillMaxWidth()) { Text(if(hindi) "सत्यापित करें" else "Verify OTP") }
            TextButton(onClick={verificationId=null;code=""}) { Text(if(hindi) "नंबर बदलें" else "Change number") }
        }
        if(status.isNotBlank()) Text(status,color=Blue)
        Text(if(hindi) "OTP कभी किसी के साथ साझा न करें।" else "Never share your OTP with anyone.",style=MaterialTheme.typography.bodySmall,color=Color.Gray)
    }
}


@Composable
fun ProfileScreen(hindi:Boolean) {
    var showOtp by remember { mutableStateOf(false) }
    var signedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
    val user=FirebaseAuth.getInstance().currentUser
    if(showOtp) {
        PhoneOtpScreen(hindi) { showOtp=false; signedIn=true }
    } else {
        Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Text(if(hindi) "प्रोफाइल और सहायता" else "Profile & support",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
            Text("Owner: Noor Alam Ansari")
            Text("Faizabad, Ayodhya, Uttar Pradesh – 224001")
            Text("8005285706 / 9335292993")
            Text("Signed in: ${user?.phoneNumber ?: "Not signed in"}")
            if(!signedIn || user==null) Button(onClick={showOtp=true},modifier=Modifier.fillMaxWidth()) { Text(if(hindi) "मोबाइल OTP से लॉगिन करें" else "Login with mobile OTP") }
            else Button(onClick={FirebaseAuth.getInstance().signOut(); signedIn=false},modifier=Modifier.fillMaxWidth()) { Text(if(hindi) "लॉग आउट" else "Sign out") }
            Text("Privacy Policy • Terms & Conditions")
            Text("WhatsApp: https://wa.me/918005285706")
        }
    }
}
