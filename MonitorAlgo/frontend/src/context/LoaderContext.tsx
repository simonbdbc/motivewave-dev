// import React from "react";
// import Spinner from "components/Spinner";
// import { createContext, useContext, useState } from "react";

// const LoaderContext = createContext();

// const LoaderContextProvider = ({ children }:any) => {
//   const [loader, setLoader] = useState(false);

//   return (
//     <LoaderContext.Provider
//       value={{
//         setLoader,
//       }}
//     >
//       {loader ? <><Spinner /><div className="bg-slate-500 static h-full w-auto z-50 blur backdrop-blur-sm">{children}</div></> : children}
//     </LoaderContext.Provider>
//   );
// };

// const useLoaderContext = () => {
//   const context = useContext(LoaderContext);

//   if (context === undefined) {
//     throw new Error("useLoaderContext must be used within GlobalContext");
//   }

//   return context;
// };

// export { LoaderContextProvider };
// export default useLoaderContext;
import type { ReactNode } from "react";
import React, { createContext, useContext, useState } from "react";
import Spinner from "components/Spinner";

// Typage du contexte
type LoaderContextType = {
    setLoader: React.Dispatch<React.SetStateAction<boolean>>;
};

// Création du contexte (null par défaut pour forcer l'utilisation du Provider)
const LoaderContext = createContext<LoaderContextType | undefined>(undefined);

// Typage des props du Provider
type LoaderContextProviderProps = {
    children: ReactNode;
};

const LoaderContextProvider: React.FC<LoaderContextProviderProps> = ({
    children,
}) => {
    const [loader, setLoader] = useState(false);

    return (
        <LoaderContext.Provider value={{ setLoader }}>
            {loader ? (
                <>
                    <Spinner />
                    <div className="bg-slate-500 static h-full w-auto z-50 blur backdrop-blur-sm">
                        {children}
                    </div>
                </>
            ) : (
                children
            )}
        </LoaderContext.Provider>
    );
};

const useLoaderContext = () => {
    const context = useContext(LoaderContext);
    if (context === undefined) {
        throw new Error(
            "useLoaderContext must be used within LoaderContextProvider"
        );
    }
    return context;
};

export { LoaderContextProvider };
export default useLoaderContext;
