import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private a = '/ptms/api';

  constructor(private h: HttpClient) {}

  dashboard() { return this.h.get<any>(`${this.a}/dashboard`); }

  departments() { return this.h.get<any[]>(`${this.a}/departments`); }
  saveDepartment(x: any) { return x.id ? this.h.put(`${this.a}/departments/${x.id}`, x) : this.h.post(`${this.a}/departments`, x); }
  deleteDepartment(id: number) { return this.h.delete(`${this.a}/departments/${id}`); }

  teams() { return this.h.get<any[]>(`${this.a}/teams`); }
  saveTeam(x: any) { return x.id ? this.h.put(`${this.a}/teams/${x.id}`, x) : this.h.post(`${this.a}/teams`, x); }
  deleteTeam(id: number) { return this.h.delete(`${this.a}/teams/${id}`); }

  employees() { return this.h.get<any[]>(`${this.a}/employees`); }
  saveEmployee(x: any) { return x.id ? this.h.put(`${this.a}/employees/${x.id}`, x) : this.h.post(`${this.a}/employees`, x); }
  deleteEmployee(id: number) { return this.h.delete(`${this.a}/employees/${id}`); }

  visitors() { return this.h.get<any[]>(`${this.a}/visitors`); }
  saveVisitor(x: any) { return x.id ? this.h.put(`${this.a}/visitors/${x.id}`, x) : this.h.post(`${this.a}/visitors`, x); }
  deleteVisitor(id: number) { return this.h.delete(`${this.a}/visitors/${id}`); }

  tickets() { return this.h.get<any[]>(`${this.a}/tickets`); }
  saveTicket(x: any) { return x.id ? this.h.put(`${this.a}/tickets/${x.id}`, x) : this.h.post(`${this.a}/tickets`, x); }
  deleteTicket(id: number) { return this.h.delete(`${this.a}/tickets/${id}`); }
  remaining() { return this.h.get<any[]>(`${this.a}/tickets/remaining`); }
  bulk(hours: number, expiry: string, barcodes: string[], startingTicketNumber?: number | null) {
    return this.h.post<any[]>(`${this.a}/tickets/bulk-scan`, { durationHours: hours, expiryDate: expiry || null, startingTicketNumber: startingTicketNumber || null, barcodes });
  }

  preview(x: any) { return this.h.post<any>(`${this.a}/issues/preview`, x); }
  createIssue(x: any) { return this.h.post<any>(`${this.a}/issues`, x); }
  createRushIssue(x: any) { return this.h.post<any>(`${this.a}/issues/rush`, x); }
  finalizeRushIssue(id: number, barcodes: string[]) { return this.h.post<any>(`${this.a}/issues/${id}/rush-finalize`, { barcodes }); }
  issues() { return this.h.get<any[]>(`${this.a}/issues`); }
  complete(id: number) { return this.h.post(`${this.a}/issues/${id}/complete`, {}); }
  cancelIssue(id: number) { return this.h.post(`${this.a}/issues/${id}/cancel`, {}); }
  updateIssue(id: number, x: any) { return this.h.put(`${this.a}/issues/${id}`, x); }
  deleteIssue(id: number) { return this.h.delete(`${this.a}/issues/${id}`); }

  reconcile(date: string, barcodes: string[]) { return this.h.post<any>(`${this.a}/reconciliations`, { date, barcodes }); }

  audit(username = '', action = '', entityType = '', from = '', to = '') {
    const p = new URLSearchParams();
    if (username) p.set('username', username);
    if (action) p.set('action', action);
    if (entityType) p.set('entityType', entityType);
    if (from) p.set('from', from);
    if (to) p.set('to', to);
    const q = p.toString();
    return this.h.get<any[]>(`${this.a}/audit${q ? '?' + q : ''}`);
  }

  settings() { return this.h.get<any>(`${this.a}/settings`); }
  saveSettings(x: any) { return this.h.put<any>(`${this.a}/settings`, x); }
}
