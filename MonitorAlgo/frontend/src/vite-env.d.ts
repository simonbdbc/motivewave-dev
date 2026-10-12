/// <reference types="vite/client" />

// Pour l'import sans suffixe
declare module "*.svg" {
    import * as React from "react";
    const ReactComponent: React.FunctionComponent<React.SVGProps<SVGSVGElement> & { title?: string }>;
    export default ReactComponent;
  }
  
  // Pour l'import avec suffixe `?react`
  declare module "*.svg?react" {
    import * as React from "react";
    const ReactComponent: React.FunctionComponent<React.SVGProps<SVGSVGElement> & { title?: string }>;
    export default ReactComponent;
  }
  