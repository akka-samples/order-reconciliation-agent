import React, { useState } from "react";
import { Container, Tabs, Tab, Box, Typography } from "@mui/material";
import Sales from "./components/Sales";
import Payment from "./components/Payment";
import Reconcile from "./components/Reconcile";
import Incident from "./components/Incident";
import Simulation from "./components/Simulation";
import { ThemeProvider, CssBaseline } from '@mui/material';
import theme from './theme';




function App() {
  const [activeTab, setActiveTab] = useState(0);

  const handleTabChange = (event, newValue) => {
    setActiveTab(newValue);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <>
        <Box
          sx={{
            backgroundColor: "#000000",
            color: "white",
            textAlign: "left", // Align the text and logo to the left
            fontSize: "18px",
            width: "100%", 
            position: "fixed", 
            top: 0,
            left: 0,
            zIndex: 1000,
            display: "flex", // Flexbox to align logo and text
            flexDirection: "column", // Stack logo on top of text
            alignItems: "around", // Align items in the center
            justifyContent: "space-between",
            minHeight: "80px", // Set a minimum height
          }}
        >
          <box style={{ display: "flex", alignItems: "center", height: "74px" }}>
            <img
              src="https://akka.io/hubfs/AKKA-2024/Images/akka_logo.svg" 
              alt="Logo"
              style={{
                width: "109px", 
                height: "25px",
                marginRight: "10px",
                marginLeft: "10px", 
              }}
            />
          </box>
          <Box className="top-bar"></Box>
        </Box>

        <Box sx={{ margin: "0 auto", marginTop: "calc(15vh + 80px)", maxWidth: "1000px",  padding: "20px" }}> 
          <Tabs value={activeTab} onChange={handleTabChange}>
            <Tab label="Simulation" />
            <Tab label="Incident" />
          </Tabs>

          {activeTab === 0 && <Simulation />}
          {activeTab === 1 && <Incident />} 
        </Box>
      </>
    </ThemeProvider>
  );
}

export default App;
