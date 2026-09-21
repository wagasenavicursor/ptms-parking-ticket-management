import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, HostListener, Input, Output } from '@angular/core';

export type SearchSelectOption = { value: string; label: string; search?: string };

@Component({
  selector: 'app-search-select',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="search-select" [class.open]="open" [class.has-query]="query.trim()" [class.no-toggle]="!showToggle">
      <input
        type="text"
        role="combobox"
        autocomplete="off"
        [attr.aria-expanded]="open"
        [attr.aria-label]="ariaLabel"
        [placeholder]="placeholder"
        [value]="open ? query : (selected?.label || '')"
        (focus)="show()"
        (input)="onInput($event)"
        (keydown)="onKeydown($event)"
      >
      <button *ngIf="showToggle" type="button" class="search-select-toggle" tabindex="-1" (click)="toggle()" aria-label="Show options">⌄</button>
      <div class="search-select-menu" *ngIf="open">
        <button type="button" class="search-select-option empty-option" *ngIf="allowEmpty" (mousedown)="$event.preventDefault();chooseEmpty()">{{emptyLabel}}</button>
        <button type="button" class="search-select-option" [class.active]="i===activeIndex" *ngFor="let option of filteredOptions; let i=index" (mousedown)="$event.preventDefault();choose(option)">
          <span class="option-main"><ng-container *ngFor="let part of highlighted(splitLabel(option.label).primary)"><mark *ngIf="part.match">{{part.text}}</mark><span *ngIf="!part.match">{{part.text}}</span></ng-container></span>
          <span class="option-secondary" *ngIf="splitLabel(option.label).secondary"><ng-container *ngFor="let part of highlighted(splitLabel(option.label).secondary)"><mark *ngIf="part.match">{{part.text}}</mark><span *ngIf="!part.match">{{part.text}}</span></ng-container></span>
        </button>
        <div class="search-select-empty" *ngIf="!filteredOptions.length">No matching results for “{{query}}”</div>
      </div>
    </div>
  `,
  styles: [`
    :host{display:block;position:relative}.search-select{position:relative}.search-select input{width:100%;padding-right:42px}.search-select.no-toggle input{padding-right:12px}.search-select.has-query input{border-color:#f59e0b;background:#fff8e6;box-shadow:0 0 0 3px rgba(245,158,11,.16);font-weight:700;color:#7c2d12}.search-select-toggle{position:absolute;right:5px;top:50%;transform:translateY(-50%);border:0;background:transparent;color:#64748b;font-size:19px;cursor:pointer;padding:6px 10px}.search-select-menu{position:absolute;z-index:1500;left:0;right:0;top:calc(100% + 5px);max-height:280px;overflow:auto;background:#fff;border:1px solid #cbd5e1;border-radius:12px;box-shadow:0 16px 36px rgba(15,23,42,.18);padding:6px}.search-select-option{display:flex;width:100%;align-items:center;justify-content:space-between;gap:14px;text-align:left;border:0;background:#fff;color:#1e293b;padding:10px 12px;border-radius:8px;cursor:pointer;font:inherit}.search-select-option:hover,.search-select-option.active{background:#eaf3ff}.option-main{font-weight:750;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.option-secondary{flex:0 0 auto;max-width:48%;padding:4px 9px;border:1px solid #bfdbfe;border-radius:999px;background:#eff6ff;color:#1d4ed8;font-size:12px;font-weight:800;letter-spacing:.02em;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.search-select-option:hover .option-secondary,.search-select-option.active .option-secondary{background:#dbeafe;border-color:#93c5fd}.search-select-option mark{background:#fde68a;color:#78350f;border-radius:3px;padding:0 2px;font-weight:800}.option-secondary mark{background:#fcd34d}.search-select-empty{padding:13px;color:#64748b;font-size:13px}.empty-option{display:block;color:#64748b;border-bottom:1px solid #e2e8f0;border-radius:0;margin-bottom:4px}@media(max-width:560px){.search-select-option{align-items:flex-start;flex-direction:column;gap:6px}.option-secondary{max-width:100%}}
  `]
})
export class SearchSelectComponent {
  @Input() options: SearchSelectOption[]=[];
  @Input() value='';
  @Output() valueChange=new EventEmitter<string>();
  @Input() placeholder='Type to search...';
  @Input() ariaLabel='Search and select';
  @Input() allowEmpty=true;
  @Input() emptyLabel='Select...';
  @Input() showToggle=true;
  query=''; open=false; activeIndex=0;
  constructor(private host:ElementRef<HTMLElement>){}
  get selected(){return this.options.find(o=>String(o.value)===String(this.value));}
  get filteredOptions(){const q=this.query.trim().toLowerCase();return this.options.filter(o=>!q||`${o.label} ${o.search||''}`.toLowerCase().includes(q));}
  show(){if(!this.open){this.query=this.selected?.label||'';this.open=true;this.activeIndex=0;}if(this.selected&&this.query===this.selected.label)this.query='';}
  toggle(){this.open?this.close():this.show();}
  close(){this.open=false;this.query=this.selected?.label||'';}
  onInput(event:Event){this.query=(event.target as HTMLInputElement).value;this.open=true;this.activeIndex=0;if(!this.query.trim()&&this.value)this.valueChange.emit('');}
  choose(option:SearchSelectOption){this.value=option.value;this.valueChange.emit(option.value);this.query=option.label;this.open=false;}
  chooseEmpty(){this.value='';this.valueChange.emit('');this.query='';this.open=false;}
  onKeydown(event:KeyboardEvent){const list=this.filteredOptions;if(event.key==='ArrowDown'){event.preventDefault();this.open=true;this.activeIndex=Math.min(this.activeIndex+1,Math.max(0,list.length-1));}else if(event.key==='ArrowUp'){event.preventDefault();this.activeIndex=Math.max(0,this.activeIndex-1);}else if(event.key==='Enter'&&this.open&&list.length){event.preventDefault();this.choose(list[this.activeIndex]||list[0]);}else if(event.key==='Escape'){event.preventDefault();this.close();}}
  highlighted(label:string){const q=this.query.trim();if(!q)return[{text:label,match:false}];const i=label.toLowerCase().indexOf(q.toLowerCase());if(i<0)return[{text:label,match:false}];return[{text:label.slice(0,i),match:false},{text:label.slice(i,i+q.length),match:true},{text:label.slice(i+q.length),match:false}].filter(p=>p.text);}
  splitLabel(label:string){const separator=label.includes(' | ')?' | ':' — ';const [primary,...secondary]=label.split(separator);return{primary,secondary:secondary.join(separator)}}
  @HostListener('document:mousedown',['$event']) outside(event:MouseEvent){if(!this.host.nativeElement.contains(event.target as Node))this.close();}
}
