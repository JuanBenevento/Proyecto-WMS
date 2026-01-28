import { Injectable, inject } from '@angular/core';
import * as fabric from 'fabric';
import { VisualData, VisualType } from '../../domain/models/visual-element.model';
import { DesignerStore } from '../../ui/store/designer.store';

@Injectable()
export class FabricCanvasService {
  public canvas!: fabric.Canvas;
  private store = inject(DesignerStore);
  private readonly GRID_SIZE = 20;
  private readonly GRID_COLOR = '#e2e8f0';
  private readonly BG_COLOR = 'transparent';
  private isDrawingWall = false;
  private wallStartPoint: { x: number, y: number } | null = null;
  private activeWall: fabric.Rect | null = null;
  private isDraggingCamera = false;
  private lastPosX = 0;
  private lastPosY = 0;

  init(canvasId: string, width: number, height: number) {
    if (this.canvas) this.canvas.dispose();

    this.canvas = new fabric.Canvas(canvasId, {
      width: width,
      height: height,
      selection: true,
      defaultCursor: 'default',
      backgroundColor: this.BG_COLOR,
      preserveObjectStacking: true,
      stopContextMenu: true,
      fireRightClick: true
    });

    this.drawInfiniteGrid();
    this.setupEvents();
  }

  resize(width: number, height: number) {
    if (this.canvas) {
      this.canvas.setDimensions({ width, height });
      this.drawInfiniteGrid();
    }
  }

  // --- GRILLA INFINITA (Corrección setBackgroundColor) ---
  private drawInfiniteGrid() {
    if (!this.canvas) return;

    const zoom = this.canvas.getZoom();
    const viewport = this.canvas.viewportTransform;
    if (!viewport) return;

    const gridSize = this.GRID_SIZE * zoom;

    const patternCanvas = document.createElement('canvas');
    patternCanvas.width = gridSize;
    patternCanvas.height = gridSize;
    const ctx = patternCanvas.getContext('2d');

    if (ctx) {
      ctx.strokeStyle = this.GRID_COLOR;
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(0.5, 0);
      ctx.lineTo(0.5, gridSize);
      ctx.moveTo(0, 0.5);
      ctx.lineTo(gridSize, 0.5);
      ctx.stroke();
    }

    const offsetX = viewport[4] % gridSize;
    const offsetY = viewport[5] % gridSize;

    const pattern = new fabric.Pattern({
      source: patternCanvas,
      repeat: 'repeat',
      offsetX: offsetX,
      offsetY: offsetY
    });

    this.canvas.backgroundColor = pattern;
    this.canvas.requestRenderAll();
  }

  private setupEvents() {
    this.canvas.on('mouse:wheel', (opt) => {
      const delta = opt.e.deltaY;
      let zoom = this.canvas.getZoom();
      zoom *= 0.999 ** delta;
      if (zoom > 20) zoom = 20;
      if (zoom < 0.05) zoom = 0.05;

      const point = new fabric.Point(opt.e.offsetX, opt.e.offsetY);
      this.canvas.zoomToPoint(point, zoom);

      opt.e.preventDefault();
      opt.e.stopPropagation();
      this.drawInfiniteGrid();
    });

    this.canvas.on('mouse:down', (opt) => {
      const evt = opt.e as MouseEvent;
      
      if (evt.altKey || evt.button === 1) {
        this.isDraggingCamera = true;
        this.canvas.selection = false;
        this.lastPosX = evt.clientX;
        this.lastPosY = evt.clientY;
        this.canvas.defaultCursor = 'grab';
      } else {
        this.onMouseDown(opt);
      }
    });

    this.canvas.on('mouse:move', (opt) => {
      const e = opt.e as MouseEvent; 

      if (this.isDraggingCamera) {
        const vpt = this.canvas.viewportTransform;
        if (!vpt) return;

        vpt[4] += e.clientX - this.lastPosX;
        vpt[5] += e.clientY - this.lastPosY;

        this.canvas.requestRenderAll();
        this.lastPosX = e.clientX;
        this.lastPosY = e.clientY;

        this.drawInfiniteGrid();
      } else {
        this.onMouseMove(opt);
      }
    });

    this.canvas.on('mouse:up', () => {
      if (this.isDraggingCamera) {
        this.isDraggingCamera = false;
        this.canvas.selection = true;
        this.canvas.defaultCursor = 'default';
        this.canvas.setViewportTransform(this.canvas.viewportTransform!);
      } else {
        this.onMouseUp();
      }
    });

    this.canvas.on('selection:created', (e) => this.store.selectObject(e.selected[0]));
    this.canvas.on('selection:updated', (e) => this.store.selectObject(e.selected[0]));
    this.canvas.on('selection:cleared', () => this.store.selectObject(null));

    this.canvas.on('object:moving', (opt) => this.snapToGrid(opt.target));
  }

  private snapToGrid(target: fabric.Object | undefined) {
    if (!target) return;
    target.set({
      left: Math.round(target.left! / this.GRID_SIZE) * this.GRID_SIZE,
      top: Math.round(target.top! / this.GRID_SIZE) * this.GRID_SIZE
    });
  }

  private onMouseDown(opt: any) {
    if (this.store.interactionMode() !== 'DRAW_WALL') return;

    this.isDrawingWall = true;
    const pointer = this.canvas.getScenePoint(opt.e);

    const startX = Math.round(pointer.x / this.GRID_SIZE) * this.GRID_SIZE;
    const startY = Math.round(pointer.y / this.GRID_SIZE) * this.GRID_SIZE;

    this.wallStartPoint = { x: startX, y: startY };

    this.activeWall = new fabric.Rect({
      left: startX,
      top: startY,
      width: this.GRID_SIZE,
      height: this.GRID_SIZE,
      fill: '#334155',
      stroke: '#1e293b',
      strokeWidth: 1,
      selectable: false,
      evented: false,
      opacity: 0.7
    });

    this.canvas.add(this.activeWall);
  }

  private onMouseMove(opt: any) {
    if (!this.isDrawingWall || !this.activeWall || !this.wallStartPoint) return;

    const pointer = this.canvas.getScenePoint(opt.e);
    const currX = Math.round(pointer.x / this.GRID_SIZE) * this.GRID_SIZE;
    const currY = Math.round(pointer.y / this.GRID_SIZE) * this.GRID_SIZE;

    const startX = this.wallStartPoint.x;
    const startY = this.wallStartPoint.y;

    const diffX = Math.abs(currX - startX);
    const diffY = Math.abs(currY - startY);

    if (diffX > diffY) {
      this.activeWall.set({
        left: Math.min(startX, currX),
        top: startY,
        width: diffX || this.GRID_SIZE,
        height: 10
      });
    } else {
      this.activeWall.set({
        left: startX,
        top: Math.min(startY, currY),
        width: 10,
        height: diffY || this.GRID_SIZE
      });
    }
    this.canvas.requestRenderAll();
  }

  private onMouseUp() {
    if (this.isDrawingWall && this.activeWall) {
      this.isDrawingWall = false;
      this.activeWall.set({ opacity: 1, selectable: true, evented: true });

      (this.activeWall as any).data = { type: 'WALL', code: 'W-NEW', status: 'UNBOUND' };

      this.canvas.setActiveObject(this.activeWall);
      this.store.selectObject(this.activeWall);

      this.activeWall = null;
      this.wallStartPoint = null;
      this.canvas.requestRenderAll();
    }
  }

  dropElement(type: VisualType, clientX: number, clientY: number) {
    const vpt = this.canvas.viewportTransform;
    if (!vpt) return;

    const canvasEl = this.canvas.getElement();
    const rect = canvasEl.getBoundingClientRect();
    const mouseX = clientX - rect.left;
    const mouseY = clientY - rect.top;

    const worldX = (mouseX - vpt[4]) / vpt[0];
    const worldY = (mouseY - vpt[5]) / vpt[3];

    this.addVisualElement(type, worldX, worldY);
  }

  addVisualElement(type: VisualType, x: number, y: number) {
    if (!this.canvas) return;

    const gridX = Math.round(x / this.GRID_SIZE) * this.GRID_SIZE;
    const gridY = Math.round(y / this.GRID_SIZE) * this.GRID_SIZE;

    let obj: fabric.Object;
    const data: VisualData = { type, code: 'NEW', status: 'UNBOUND' };

    if (type === 'RACK') {
      const rect = new fabric.Rect({ width: 40, height: 80, fill: '#e0e7ff', stroke: '#4338ca', strokeWidth: 2, rx: 2 });
      const text = new fabric.Text('???', { fontSize: 12, fontFamily: 'monospace', fill: '#4338ca', originX: 'center', originY: 'center', left: 20, top: 40 });
      obj = new fabric.Group([rect, text], { left: gridX, top: gridY });
    } else if (type === 'ZONE') {
      obj = new fabric.Rect({
        width: 100, height: 100,
        fill: 'rgba(16, 185, 129, 0.2)', stroke: '#10b981', strokeDashArray: [5, 5],
        left: gridX, top: gridY,
        transparentCorners: false, cornerColor: '#10b981'
      });
      obj.setControlsVisibility({ mtr: false });
    } else { return; }

    (obj as any).data = data;
    this.canvas.add(obj);
    this.canvas.setActiveObject(obj);
    this.store.selectObject(obj);
    this.canvas.requestRenderAll();
  }

  setReadOnly(readOnly: boolean) {
    if (!this.canvas) return;
    this.canvas.selection = !readOnly;
    this.canvas.forEachObject((obj) => {
      obj.set({
        selectable: !readOnly,
        evented: true,
        lockMovementX: readOnly,
        lockMovementY: readOnly,
        lockScalingX: readOnly,
        lockScalingY: readOnly,
        lockRotation: readOnly
      });
    });
    this.canvas.requestRenderAll();
  }

  getAllRackCodes(): string[] {
    if (!this.canvas) return [];
    return this.canvas.getObjects()
      .map((o: any) => o.data as VisualData)
      .filter(data => data && data.type === 'RACK' && data.code && data.code !== 'NEW' && data.code !== '???')
      .map(data => data.code);
  }

  updateObjectStatus(code: string, status: string) {
    if (!this.canvas) return;
    const object = this.canvas.getObjects().find((obj: any) => obj.data?.code === code);
    if (!object) return;

    let fillColor = '#e0e7ff';
    let strokeColor = '#4338ca';

    switch (status) {
      case 'EMPTY': fillColor = '#dcfce7'; strokeColor = '#166534'; break;
      case 'PARTIAL': fillColor = '#fef9c3'; strokeColor = '#ca8a04'; break;
      case 'FULL': fillColor = '#fee2e2'; strokeColor = '#991b1b'; break;
      case 'OVERLOADED': fillColor = '#7f1d1d'; strokeColor = '#ff0000'; break;
      case 'UNBOUND': fillColor = '#f3f4f6'; strokeColor = '#9ca3af'; break;
    }

    if (object instanceof fabric.Group && (object as any).data.type === 'RACK') {
      const rect = object.getObjects().find(o => o instanceof fabric.Rect);
      if (rect) rect.set({ fill: fillColor, stroke: strokeColor });
    } else {
      object.set({ fill: fillColor, stroke: strokeColor });
    }
    this.canvas.requestRenderAll();
  }

  updateSelectedData(newData: VisualData) {
    const active = this.canvas.getActiveObject();
    if (!active) return;
    (active as any).data = newData;
    if (active instanceof fabric.Group && newData.type === 'RACK') {
      const textObj = active.getObjects().find(o => o instanceof fabric.Text) as fabric.Text;
      if (textObj) textObj.set('text', newData.code);
    }
    this.canvas.requestRenderAll();
  }

  deleteSelected() {
    const active = this.canvas.getActiveObjects();
    if (active.length) {
      this.canvas.discardActiveObject();
      active.forEach(o => this.canvas.remove(o));
      this.canvas.requestRenderAll();
      this.store.selectObject(null);
    }
  }

  toJSON() { return JSON.stringify(this.canvas.toObject(['data'])); }
  loadJSON(json: string) {
    this.canvas.loadFromJSON(json).then(() => {
      this.canvas.requestRenderAll();
      this.drawInfiniteGrid(); 
  });
}

  isCodeInUse(code: string, excludeObject: fabric.Object | null | undefined): boolean {
    if (!this.canvas || !code) return false;

    const searchCode = code.toUpperCase().trim();

    return this.canvas.getObjects().some((obj: any) => {
      if (excludeObject && obj === excludeObject) return false;

      const objCode = obj.data?.code?.toUpperCase();
      return objCode === searchCode;
    });
  }

  getActiveObject(): fabric.Object | undefined {
    return this.canvas?.getActiveObject();
  }
}