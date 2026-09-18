import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import { AppComponent } from './app.component';

@Component({selector:'ptms-shell',standalone:true,imports:[CommonModule,FormsModule,AppComponent],templateUrl:'./shell.component.html',styleUrls:['./shell.component.css']})
export class ShellComponent {
  user:any=null; username='admin'; password='admin'; email=''; loginError=''; message=''; userError=''; userMessage=''; forgot=false; users:any[]=[]; userRoleFilter='ALL'; manage=false; editing:any=this.blank(); loading=false; loadingText='Loading ParkingTiq...'; confirmDialog:any=null; private confirmResolver:((value:boolean)=>void)|null=null;
  constructor(private h:HttpClient){}
  get filteredUsers(){return this.userRoleFilter==='ALL'?this.users:this.users.filter(u=>u.role===this.userRoleFilter);}
  roleCount(role:string){return role==='ALL'?this.users.length:this.users.filter(u=>u.role===role).length;}
  blank(){return{fullName:'',username:'',email:'',role:'SECURITY',enabled:true,password:'',profileImage:''}}
  busy(text:string){this.loadingText=text;this.loading=true}
  login(){this.loginError='';const started=Date.now();this.busy('Signing in securely...');const finish=(action:()=>void)=>setTimeout(()=>{action();this.loading=false},Math.max(0,3000-(Date.now()-started)));this.h.post<any>('/ptms/api/auth/login',{username:this.username,password:this.password}).subscribe({next:u=>finish(()=>{this.user=u;if(u.mustChangePassword)this.message='Default password detected. Change it from the account menu.'}),error:e=>finish(()=>this.loginError=e?.error?.message||'Invalid username or password')});}
  logout(){this.busy('Signing out...');this.h.post('/ptms/api/auth/logout',{}).pipe(finalize(()=>this.loading=false)).subscribe({next:()=>{this.user=null;this.manage=false;this.password='';}})}
  forgotPassword(){if(!this.email){this.loginError='Enter your account email first';return}this.busy('Preparing password reset...');this.h.post<any>('/ptms/api/auth/forgot-password',{email:this.email}).pipe(finalize(()=>this.loading=false)).subscribe({next:r=>{this.message=r.message;this.forgot=false},error:e=>this.loginError=e?.error?.message||'Unable to process request'});}
  openUsers(){this.manage=true;this.userError='';this.userMessage='';this.loadUsers()}
  loadUsers(){this.busy('Loading users...');this.h.get<any[]>('/ptms/api/users').pipe(finalize(()=>this.loading=false)).subscribe({next:x=>{this.users=x;this.userError='';},error:e=>this.userError=e?.error?.message||'Unable to load users'});}
  edit(u:any){this.userError='';this.userMessage='';this.editing={...u,password:''};}
  async saveUser(){const action=this.editing.id?'update':'create';if(!(await this.askConfirm(action==='update'?'Update User':'Create User',`${action==='update'?'Update':'Create'} user ${this.editing.fullName||this.editing.username}?`,action==='update'?'Update User':'Create User','success')))return;this.userError='';this.userMessage='';this.busy('Saving user...');const req=this.editing.id?this.h.put<any>(`/ptms/api/users/${this.editing.id}`,this.editing):this.h.post<any>('/ptms/api/users',this.editing);req.pipe(finalize(()=>this.loading=false)).subscribe({next:(saved:any)=>{const idx=this.users.findIndex(u=>u.id===saved.id);if(idx>=0)this.users=[...this.users.slice(0,idx),saved,...this.users.slice(idx+1)];else this.users=[...this.users,saved];this.users=[...this.users].sort((a,b)=>(a.id||0)-(b.id||0));if(this.user?.id===saved.id)this.user={...this.user,...saved};this.editing=this.blank();this.userMessage=`User saved successfully. ${this.users.length} user(s) currently loaded.`;this.loadUsers();},error:(e:any)=>this.userError=e?.error?.message||'User was not saved'});}
  async remove(u:any){if(await this.askConfirm('Delete User',`Delete user ${u.fullName}? This cannot be undone.`,'Delete User','danger')){this.userError='';this.userMessage='';this.busy('Removing user...');this.h.delete(`/ptms/api/users/${u.id}`).pipe(finalize(()=>this.loading=false)).subscribe({next:()=>{this.userMessage='User deleted';this.loadUsers();},error:e=>this.userError=e?.error?.message||'Unable to delete user'});}}
  askConfirm(title:string,message:string,confirmLabel='Confirm',tone:'default'|'danger'|'success'='default'){this.confirmDialog={title,message,confirmLabel,tone};return new Promise<boolean>(resolve=>this.confirmResolver=resolve);}
  resolveConfirm(value:boolean){const r=this.confirmResolver;this.confirmDialog=null;this.confirmResolver=null;r?.(value);}
  profileSelected(event:Event){const input=event.target as HTMLInputElement;const file=input.files?.[0];if(!file)return;if(!file.type.startsWith('image/')){this.userError='Please select an image file';input.value='';return;}if(file.size>1_500_000){this.userError='Profile picture must be smaller than 1.5 MB';input.value='';return;}const reader=new FileReader();reader.onload=()=>{this.editing.profileImage=String(reader.result||'');this.userError='';};reader.onerror=()=>this.userError='Unable to read profile picture';reader.readAsDataURL(file);}
  removeProfileImage(){this.editing.profileImage='';}
}
