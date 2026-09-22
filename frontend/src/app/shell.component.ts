import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import { AppComponent } from './app.component';
import { SortableTableDirective } from './core/sortable-table.directive';

@Component({selector:'ptms-shell',standalone:true,imports:[CommonModule,FormsModule,AppComponent,SortableTableDirective],templateUrl:'./shell.component.html',styleUrls:['./shell.component.css']})
export class ShellComponent {
  user:any=null; username=''; password=''; rememberMe=false; email=''; loginError=''; message=''; userError=''; userMessage=''; forgot=false; users:any[]=[]; userRoleFilter='ALL'; manage=false; editing:any=this.blank(); loading=false; loadingText='Loading ParkingTiq...'; confirmDialog:any=null; private confirmResolver:((value:boolean)=>void)|null=null;
  readonly permissionItems=[{key:'home',label:'Home'},{key:'dashboard',label:'Dashboard'},{key:'issue',label:'Issue Parking Tickets'},{key:'register',label:'Issued Tickets Register'},{key:'employees',label:'Employees'},{key:'visitors',label:'Visitors'},{key:'inventory',label:'Ticket Inventory'},{key:'remaining',label:'Remaining Inventory'},{key:'reports',label:'Reports'},{key:'settings',label:'Settings'}];
  navigationDefaults:any={ADMIN:[],SECURITY:[]};
  constructor(private h:HttpClient){
    try{
      const remembered=localStorage.getItem('parkingtiq.rememberedUsername');
      this.rememberMe=localStorage.getItem('parkingtiq.rememberLogin')==='true';
      if(remembered)this.username=remembered;
      if(this.rememberMe)this.restoreSession();
    }catch{}
  }
  @HostListener('window:keydown',['$event']) onShellKeyboard(event:KeyboardEvent){
    if(event.key!=='Escape')return;
    if(this.confirmDialog){event.preventDefault();this.resolveConfirm(false);return;}
    if(this.manage){event.preventDefault();this.manage=false;return;}
  }
  get filteredUsers(){return this.userRoleFilter==='ALL'?this.users:this.users.filter(u=>u.role===this.userRoleFilter);}
  roleCount(role:string){return role==='ALL'?this.users.length:this.users.filter(u=>u.role===role).length;}
  blank(){return{fullName:'',username:'',email:'',role:'SECURITY',enabled:true,password:'',passwordConfirm:'',profileImage:'',useCustomPermissions:false,navigationPermissions:[]}}
  busy(text:string){this.loadingText=text;this.loading=true}
  login(){this.loginError='';this.message='';const started=Date.now();this.busy('Signing in securely...');const finish=(action:()=>void)=>setTimeout(()=>{action();this.loading=false},Math.max(0,3000-(Date.now()-started)));this.h.post<any>('/ptms/api/auth/login',{username:this.username,password:this.password,rememberMe:this.rememberMe}).subscribe({next:u=>finish(()=>{this.user=u;this.message=u.mustChangePassword?'Default password detected. Change it from the account menu.':'';this.saveRememberedLogin();}),error:e=>finish(()=>this.loginError=e?.error?.message||'Invalid username or password')});}
  saveRememberedLogin(){try{if(this.rememberMe&&this.username.trim()){localStorage.setItem('parkingtiq.rememberedUsername',this.username.trim());localStorage.setItem('parkingtiq.rememberLogin','true');}else{localStorage.removeItem('parkingtiq.rememberedUsername');localStorage.removeItem('parkingtiq.rememberLogin');}}catch{}}
  rememberChanged(){if(!this.rememberMe){try{localStorage.removeItem('parkingtiq.rememberedUsername');localStorage.removeItem('parkingtiq.rememberLogin');}catch{}}}
  restoreSession(){this.busy('Restoring your session...');this.h.get<any>('/ptms/api/auth/me').pipe(finalize(()=>this.loading=false)).subscribe({next:u=>{this.user=u;this.username=u.username||this.username;this.message=u.mustChangePassword?'Default password detected. Change it from the account menu.':'';},error:()=>{this.user=null;}});}
  logout(){this.busy('Signing out...');this.h.post('/ptms/api/auth/logout',{}).pipe(finalize(()=>this.loading=false)).subscribe({next:()=>{this.user=null;this.manage=false;this.password='';if(!this.rememberMe)this.username='';}})}
  forgotPassword(){if(!this.email){this.loginError='Enter your account email first';return}this.busy('Preparing password reset...');this.h.post<any>('/ptms/api/auth/forgot-password',{email:this.email}).pipe(finalize(()=>this.loading=false)).subscribe({next:r=>{this.message=r.message;this.forgot=false},error:e=>this.loginError=e?.error?.message||'Unable to process request'});}
  openUsers(){this.manage=true;this.userError='';this.userMessage='';this.editing=this.blank();this.loadUsers();this.loadNavigationDefaults()}
  loadUsers(){this.busy('Loading users...');this.h.get<any[]>('/ptms/api/users').pipe(finalize(()=>this.loading=false)).subscribe({next:x=>{this.users=x;this.userError='';},error:e=>this.userError=e?.error?.message||'Unable to load users'});}
  edit(u:any){this.userError='';this.userMessage='';this.editing={...u,password:'',passwordConfirm:'',useCustomPermissions:Array.isArray(u.navigationPermissions),navigationPermissions:[...(u.navigationPermissions||[])]};}
  async saveUser(){
    this.userError='';this.userMessage='';
    if(!this.editing.fullName?.trim()){this.userError='Enter the user full name';return}
    if(!this.editing.username?.trim()){this.userError='Enter a username';return}
    if(!this.editing.email?.trim()){this.userError='Enter an email address';return}
    if(!this.editing.id&&!this.editing.password){this.userError='Enter a password for the new user';return}
    if((this.editing.password||this.editing.passwordConfirm)&&this.editing.password!==this.editing.passwordConfirm){this.userError='Password and confirmation do not match';return}
    const action=this.editing.id?'update':'create';if(!(await this.askConfirm(action==='update'?'Update User':'Create User',`${action==='update'?'Update':'Create'} user ${this.editing.fullName||this.editing.username}?`,action==='update'?'Update User':'Create User','success')))return;
    const payload={...this.editing,navigationPermissions:this.editing.useCustomPermissions?this.editing.navigationPermissions:null};
    this.busy('Saving user...');const req=this.editing.id?this.h.put<any>(`/ptms/api/users/${this.editing.id}`,payload):this.h.post<any>('/ptms/api/users',payload);req.pipe(finalize(()=>this.loading=false)).subscribe({next:(saved:any)=>{const idx=this.users.findIndex(u=>u.id===saved.id);if(idx>=0)this.users=[...this.users.slice(0,idx),saved,...this.users.slice(idx+1)];else this.users=[...this.users,saved];this.users=[...this.users].sort((a,b)=>(a.id||0)-(b.id||0));if(this.user?.id===saved.id){this.user={...this.user,...saved};if(!saved.mustChangePassword)this.message='';}this.editing=this.blank();this.userMessage=`User saved successfully. ${this.users.length} user(s) currently loaded.`;this.loadUsers();},error:(e:any)=>this.userError=e?.error?.message||'User was not saved'});
  }
  async remove(u:any){if(await this.askConfirm('Delete User',`Delete user ${u.fullName}? This cannot be undone.`,'Delete User','danger')){this.userError='';this.userMessage='';this.busy('Removing user...');this.h.delete(`/ptms/api/users/${u.id}`).pipe(finalize(()=>this.loading=false)).subscribe({next:()=>{this.userMessage='User deleted';this.loadUsers();},error:e=>this.userError=e?.error?.message||'Unable to delete user'});}}
  askConfirm(title:string,message:string,confirmLabel='Confirm',tone:'default'|'danger'|'success'='default'){this.confirmDialog={title,message,confirmLabel,tone};return new Promise<boolean>(resolve=>this.confirmResolver=resolve);}
  resolveConfirm(value:boolean){const r=this.confirmResolver;this.confirmDialog=null;this.confirmResolver=null;r?.(value);}
  profileSelected(event:Event){const input=event.target as HTMLInputElement;const file=input.files?.[0];if(!file)return;if(!file.type.startsWith('image/')){this.userError='Please select an image file';input.value='';return;}if(file.size>1_500_000){this.userError='Profile picture must be smaller than 1.5 MB';input.value='';return;}const reader=new FileReader();reader.onload=()=>{this.editing.profileImage=String(reader.result||'');this.userError='';};reader.onerror=()=>this.userError='Unable to read profile picture';reader.readAsDataURL(file);}
  removeProfileImage(){this.editing.profileImage='';}
  loadNavigationDefaults(){this.h.get<any>('/ptms/api/users/navigation-defaults').subscribe({next:x=>this.navigationDefaults=x,error:e=>this.userError=e?.error?.message||'Unable to load menu access settings'});}
  hasPermission(list:string[],key:string){return list?.includes(key)}
  togglePermission(list:string[],key:string,checked:boolean){if(key==='home')return;const values=new Set(list||[]);checked?values.add(key):values.delete(key);if(!values.has('home'))values.add('home');return [...values]}
  permissionChanged(scope:'USER'|'ADMIN'|'SECURITY',key:string,event:Event){const checked=(event.target as HTMLInputElement).checked;if(scope==='USER')this.editing.navigationPermissions=this.togglePermission(this.editing.navigationPermissions,key,checked);else this.navigationDefaults[scope]=this.togglePermission(this.navigationDefaults[scope],key,checked);}
  async saveNavigationDefaults(){if(!(await this.askConfirm('Save Role Menu Access','Apply these default menu permissions to Admin and Security users who do not have individual overrides?','Save Access','success')))return;this.userError='';this.userMessage='';this.busy('Saving menu access...');this.h.put<any>('/ptms/api/users/navigation-defaults',this.navigationDefaults).pipe(finalize(()=>this.loading=false)).subscribe({next:x=>{this.navigationDefaults=x;this.userMessage='Default menu access saved successfully.';},error:e=>this.userError=e?.error?.message||'Menu access settings were not saved'});}
}
