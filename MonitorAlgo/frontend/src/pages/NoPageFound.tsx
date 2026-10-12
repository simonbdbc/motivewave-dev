import { useEffect } from "react";
import ImageError404 from "assets/img/error-404.jpeg";
import { useNavigate } from "react-router-dom";

const NoPageFound = () => {
  const navigate = useNavigate();

  useEffect(() => {
    setTimeout(() => {
      navigate("/");
    }, 1000);
  }, [navigate]);

  return (
    <>
      <section className="h-screen bg-black">
        <div className="pt-24">
          <div className="container px-3 mx-auto flex items-center">
            <div className="flex w-full justify-center items-center text-center">
              <img className="w-1/2" alt="Erreur 404" src={ImageError404} />
            </div>
          </div>
        </div>
      </section>
    </>
  );
};
export default NoPageFound;
