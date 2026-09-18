import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import { AppComponent } from './app.component';

@Component({selector:'ptms-shell',standalone:true,imports:[CommonModule,FormsModule,AppComponent],templateUrl:'./shell.component.html',styleUrls:['./shell.component.css']})
export class ShellComponent {
  user:any=null; username='admin'; password='admin'; email=''; loginError=''; message=''; forgot=false; users:any[]=[]; manage=false; editing:any=this.blank(); loading=false; loadingText='Loading ParkingTIQ...';
  constructor(private h:HttpClient){}
  blank(){return{fullName:'',username:'',email:'',role:'SECURITY',enabled:true,password:''}}
  busy(text:string){this.loadingText=text;this.loading=true}
  login(){this.loginError='';this.busy('Signing in securely...');this.h.post<any>('/ptms/api/auth/login',{username:this.username,password:this.password}).pipe(finalize(()=>this.loading=false)).subscribe({next:u=>{this.user=u;if(u.mustChangePassword)this.message='Default password detected. Change it from the account menu.'},error:e=>this.loginError=e?.error?.message||'Invalid username or password'});}
  logout(){this.busy('Signing out...');this.h.post('/ptms/api/auth/logout',{}).pipe(finalize(()=>this.loading=false)).subscribe({next:()=>{this.user=null;this.manage=false;this.password='';}})}
  forgotPassword(){if(!this.email){this.loginError='Enter your account email first';return}this.busy('Preparing password reset...');this.h.post<any>('/ptms/api/auth/forgot-password',{email:this.email}).pipe(finalize(()=>this.loading=false)).subscribe({next:r=>{this.message=r.message;this.forgot=false},error:e=>this.loginError=e?.error?.message||'Unable to process request'});}
  openUsers(){this.manage=true;this.loadUsers()}
  loadUsers(){this.busy('Loading users...');this.h.get<any[]>('/ptms/api/users').pipe(finalize(()=>this.loading=false)).subscribe({next:x=>this.users=x,error:e=>this.loginError=e?.error?.message||'Unable to load users'});}
  edit(u:any){this.editing={...u,password:''};}
  saveUser(){const action=this.editing.id?'update':'create';if(!confirm(`${action==='update'?'Update':'Create'} user ${this.editing.fullName||this.editing.username}?`))return;this.busy('Saving user...');const req=this.editing.id?this.h.put<any>(`/ptms/api/users/${this.editing.id}`,this.editing):this.h.post<any>('/ptms/api/users',this.editing);req.pipe(finalize(()=>this.loading=false)).subscribe({next:(saved:any)=>{const idx=this.users.findIndex(u=>u.id===saved.id);if(idx>=0)this.users=[...this.users.slice(0,idx),saved,...this.users.slice(idx+1)];else this.users=[...this.users,saved];this.users=[...this.users].sort((a,b)=>(a.id||0)-(b.id||0));this.editing=this.blank();this.message='User saved';this.loadUsers();},error:(e:any)=>this.loginError=e?.error?.message||'Unable to save user'});}
  remove(u:any){if(confirm(`Delete user ${u.fullName}? This cannot be undone.`)){this.busy('Removing user...');this.h.delete(`/ptms/api/users/${u.id}`).pipe(finalize(()=>this.loading=false)).subscribe({next:()=>this.loadUsers(),error:e=>this.loginError=e?.error?.message||'Unable to delete user'});}}
}
