import React,{useEffect,useState} from 'react';
import{createRoot}from'react-dom/client';
import{initializeApp}from'firebase/app';
import{getAuth,signInWithEmailAndPassword,onAuthStateChanged,signOut}from'firebase/auth';
import{getFirestore,collection,getDocs,doc,getDoc,updateDoc,query,orderBy}from'firebase/firestore';
import{getStorage,ref,getDownloadURL}from'firebase/storage';
import'./style.css';
const cfg={apiKey:import.meta.env.VITE_FIREBASE_API_KEY,authDomain:import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,projectId:import.meta.env.VITE_FIREBASE_PROJECT_ID,storageBucket:import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,messagingSenderId:import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,appId:import.meta.env.VITE_FIREBASE_APP_ID};
const app=initializeApp(cfg),auth=getAuth(app),db=getFirestore(app),storage=getStorage(app);
function App(){
 const[user,setUser]=useState(null),[admin,setAdmin]=useState(false),[email,setEmail]=useState(''),[pass,setPass]=useState(''),[rows,setRows]=useState([]),[services,setServices]=useState([]),[customers,setCustomers]=useState([]),[err,setErr]=useState('');
 async function load(){try{const s=await getDocs(query(collection(db,'applications'),orderBy('createdAt','desc')));setRows(s.docs.map(d=>({id:d.id,...d.data()})));const sv=await getDocs(collection(db,'services'));setServices(sv.docs.map(d=>({id:d.id,...d.data()})));const cu=await getDocs(collection(db,'users'));setCustomers(cu.docs.map(d=>({id:d.id,...d.data()}))); }catch(e){setErr(e.message)}}
 useEffect(()=>onAuthStateChanged(auth,async u=>{setUser(u);if(u){const p=await getDoc(doc(db,'users',u.uid));const ok=p.exists()&&p.data().role==='admin';setAdmin(ok);if(ok)load();else setErr('This account is not an admin. Set role=admin in Firestore using trusted console.');}}),[]);
 async function login(e){e.preventDefault();setErr('');try{await signInWithEmailAndPassword(auth,email,pass)}catch(x){setErr(x.message)}}
 async function status(id,value){await updateDoc(doc(db,'applications',id),{status:value,updatedAt:new Date()});load()}
 async function savePrice(id,value){const amount=Number(value);if(!Number.isFinite(amount)||amount<0){setErr('Enter a valid non-negative price');return}await updateDoc(doc(db,'services',id),{price:amount});load()}
 if(!user)return <main className="login"><h1>Noor Alam Jan Seva Kendra</h1><p>Admin Login</p><form onSubmit={login}><input placeholder="Admin email" value={email} onChange={e=>setEmail(e.target.value)}/><input type="password" placeholder="Password" value={pass} onChange={e=>setPass(e.target.value)}/><button>Sign in</button></form>{err&&<p className="error">{err}</p>}</main>;
 if(!admin)return <main><h2>Access denied</h2><p>{err}</p><button onClick={()=>signOut(auth)}>Sign out</button></main>;
 return <main><header><div><h1>Noor Alam Jan Seva Kendra</h1><p>Application Management Dashboard</p></div><button onClick={()=>signOut(auth)}>Logout</button></header>
 <section className="stats"><article><b>{rows.length}</b><span>Total applications</span></article><article><b>{rows.filter(x=>x.status==='submitted').length}</b><span>New requests</span></article><article><b>{rows.filter(x=>x.status==='completed').length}</b><span>Completed</span></article></section>
 {err&&<p className="error">{err}</p>}<h2>Applications</h2><div className="tablewrap"><table><thead><tr><th>Customer</th><th>Phone</th><th>Service</th><th>Details</th><th>Documents</th><th>Status</th><th>Update</th></tr></thead><tbody>{rows.map(r=><tr key={r.id}><td>{r.customerName}</td><td>{r.phone}</td><td>{r.serviceName}</td><td>{r.details}</td><td>{(r.documentPaths||[]).map((p,i)=><div key={p}><button onClick={async()=>{try{const u=await getDownloadURL(ref(storage,p));window.open(u,'_blank','noopener')}}>Open document {i+1}</button></div>)}</td><td>{r.status}</td><td><select value={r.status||'submitted'} onChange={e=>status(r.id,e.target.value)}>{['submitted','under_review','accepted','rejected','in_progress','completed'].map(s=><option key={s}>{s}</option>)}</select></td></tr>)}</tbody></table></div>
 <h2>Service catalogue</h2><p>Service prices can be maintained in Firestore collection <code>services</code>. This starter displays configured service records.</p>{services.map(s=><div className="serviceRow" key={s.id}><span>{s.nameEn||s.nameHi||s.id}</span><label>₹ <input aria-label={'Price for '+s.id} type="number" min="0" defaultValue={s.price??''} placeholder="Price" onBlur={e=>savePrice(s.id,e.target.value)}/></label><span>{s.active===false?'Inactive':'Active'}</span></div>)}
 <h2>Customers</h2><div className="tablewrap"><table><thead><tr><th>Phone</th><th>Role</th><th>Preferred language</th></tr></thead><tbody>{customers.map(c=><tr key={c.id}><td>{c.phone||c.email||c.id}</td><td>{c.role||'customer'}</td><td>{c.preferredLanguage||'—'}</td></tr>)}</tbody></table></div>
 </main>
}
createRoot(document.getElementById('root')).render(<App/>);
