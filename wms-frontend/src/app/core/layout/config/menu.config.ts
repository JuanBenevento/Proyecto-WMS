export interface MenuItem {
  label: string;
  icon: string;
  route: string;
  category?: string;
}

export const SAAS_MENU: MenuItem[] = [
  { category: 'Plataforma', 
    label: 'Empresas', 
    icon: 'bi-buildings-fill', 
    route: '/saas/tenants' } 
];

export const ADMIN_MENU: MenuItem[] = [
  { 
    category: 'General', 
    label: 'Mapa Físico Depósito', 
    icon: 'bi-speedometer2', 
    route: '/admin/dashboard' 
  },

  // Operativa Diaria 
  { 
    category: 'Operativa Diaria', 
    label: 'Recepción (Inbound)', 
    icon: 'bi-box-arrow-in-down', 
    route: '/admin/receive'
  },
  { 
    label: 'Movimientos Internos', 
    icon: 'bi-arrows-move', 
    route: '/admin/move' 
  },
  { 
    label: 'Picking y Despacho', 
    icon: 'bi-truck', 
    route: '/admin/picking' 
  },

  // Gestión y Control
  { 
    category: 'Control de Stock', 
    label: 'Stock Actual', 
    icon: 'bi-box-seam-fill', 
    route: '/admin/inventory' 
  },
  { 
    label: 'Auditoría (Logs)', 
    icon: 'bi-shield-check', 
    route: '/admin/audit' 
  },

  // Maestros
  { 
    category: 'Maestros y Configuración', 
    label: 'Diseño de Depósito',  
    icon: 'bi-map-fill',          
    route: '/admin/warehouse/designer'
  },
  { 
    label: 'Productos', 
    icon: 'bi-tags-fill', 
    route: '/admin/products' 
  },
  { 
    label: 'Ubicaciones', 
    icon: 'bi-grid-3x3-gap-fill', 
    route: '/admin/locations' 
  },
  { 
    label: 'Usuarios', 
    icon: 'bi-people-fill', 
    route: '/admin/users' 
  }
];

export const OPERATOR_MENU: MenuItem[] = [
  { category: 'Tareas', label: 'Recepción', icon: 'bi-box-arrow-in-down', route: '/operation/receive' },
  { label: 'Movimientos', icon: 'bi-arrows-move', route: '/operation/move' },
  { label: 'Despacho', icon: 'bi-truck', route: '/operation/dispatch' },
  { category: 'Control de Stock', label: 'Stock Actual', icon: 'bi-box-seam-fill', route: '/operation/inventory' }
];