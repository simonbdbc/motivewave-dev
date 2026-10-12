import { Suspense } from "react";
import { BrowserRouter as Router } from "react-router-dom";
import Routes from "router/Routes";
import Spinner from "components/Spinner";
// import useEvm from "hooks/useEvm";

// import { SidebarContext } from 'context/SidebarContext'
// import ThemeContext, { ThemeContextProvider } from "./state/ThemeContext.js"
// import { WindowSizeContextProvider } from "./state/WindowSizeContext.js"

const App = () => {
  // const { autoConnectWallet } = useEvm();

  // useEffect(() => {
  //     const fetchAutoConnectWallet = async () => {
  //       await autoConnectWallet();
  //     }
  //     // call the function make sure to catch any error
  //     fetchAutoConnectWallet().catch(console.error);
  // }, []);

  return (
    <Router>
      <Suspense fallback={<Spinner />}>
        <Routes />
      </Suspense>
    </Router>
  );
};
export default App;
