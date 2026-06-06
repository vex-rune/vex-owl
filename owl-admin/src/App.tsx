import React from 'react';
import { RouterProvider } from 'react-router-dom';
import { router } from './router';
import { GlobalStyles } from './core/theme/appTheme';

const App: React.FC = () => {
  return (
    <>
      <GlobalStyles />
      <RouterProvider router={router} />
    </>
  );
};

export default App;