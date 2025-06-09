// theme.js
import { createTheme } from "@mui/material/styles";

const theme = createTheme({
  palette: {
    primary: {
      main: "#00dbdd",
    },
    secondary: {
      main: "#ffce4a",
    },
    text: {
      primary: "#ffffff",
      secondary: "#ffffff",
    },
    background: {
      default: "#000",
    },
  },
  typography: {
    fontFamily: '"Instrument Sans", sans-serif', // Set a global font family
    h1: {
      fontSize: "2.5rem",
      fontWeight: 700,
      letterSpacing: "-0.01562em",
    },
    h2: {
      fontSize: "2rem",
      fontWeight: 700,
      letterSpacing: "-0.00833em",
    },
    h3: {
      fontSize: "1.75rem",
      fontWeight: 600,
    },
    h4: {
      fontSize: "1.5rem",
      fontWeight: 600,
    },
    h5: {
      fontSize: "1.25rem",
      fontWeight: 500,
    },
    h6: {
      fontSize: "1rem",
      fontWeight: 500,
    },
    subtitle1: {
      fontSize: "1rem",
      fontWeight: 400,
    },
    subtitle2: {
      fontSize: "0.875rem",
      fontWeight: 400,
    },
    body1: {
      fontSize: "1rem",
      fontWeight: 400,
    },
    body2: {
      fontSize: "0.875rem",
      fontWeight: 400,
    },
    button: {
      textTransform: "none", // Prevent uppercase conversion
      fontWeight: 600,
    },
    caption: {
      fontSize: "0.75rem",
      fontWeight: 400,
    },
    overline: {
      fontSize: "0.75rem",
      fontWeight: 400,
      letterSpacing: "0.1em",
      textTransform: "uppercase",
    },
  },
  components: {
    MuiTabs: {
      styleOverrides: {
        indicator: {
          backgroundColor: "#ffce4a", // Example: primary color
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: {
          backgroundColor: "#0D0D0D", // Example: background color when selected
          "&.Mui-selected": {
            color: "#ffce4a", // Example: primary color when selected
            backgroundColor: "#1a1a1a", // Example: background color when selected
          },
        },
      },
    },
    // Override styles for TextField container
    MuiTextField: {
      styleOverrides: {
        root: {
          marginBottom: "16px",
        },
      },
    },
    // Override styles for the Input Label
    MuiInputLabel: {
      styleOverrides: {
        root: {
          fontSize: "1.5rem",
          color: "#aaa", // Custom label color
        },
        outlined: {
          "&.MuiInputLabel-shrink": {
            fontSize: "1.5rem",
            transform: "translate(14px, -14px) scale(0.75)",
          },
        },
      },
    },
    // Override styles for Outlined Input (used by TextField variant "outlined")
    MuiOutlinedInput: {
      styleOverrides: {
        notchedOutline: {
          fontSize: "1.5rem",
        },
        root: {
          // Style the border (notched outline)
          "& .MuiOutlinedInput-notchedOutline": {
            borderColor: "#aaa",
          },
          "&:hover .MuiOutlinedInput-notchedOutline": {
            borderColor: "#aaa",
          },
          "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
            borderColor: "#00dbdd",
          },
        },
        // Style the input text and background
        input: {
          color: "#fff", // Input text color set to white
          backgroundColor: "#1a1a1a", // Input background color (for contrast)
          padding: "20px",
        },
      },
    },
    // Override styles for Buttons
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: "uppercase", // Keep button text as-is (not uppercase)
          padding: "12px 24px",
          backgroundColor: "#00dbdd", // Custom background color
          color: "#000", // Button text color white
          border: "1px solid #00dbdd", // Custom border color
          borderRadius: "0", // Remove border
          "&:hover": {
            backgroundColor: "#000", // Change background color on hover
            color: "#00dbdd", // Change text color on hover
          },
        },
      },
    },
    // Override the Dialog's paper (container) styles
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: "8px",
          backgroundColor: "#1a1a1a", // Customize background color as needed
          padding: "20px",
          color: "#fff", // Text color
        },
      },
    },
    // Override the DialogTitle styles
    MuiDialogTitle: {
      styleOverrides: {
        root: {
          fontSize: "1.5rem",
          fontWeight: 600,
          padding: "16px",
          backgroundColor: "#1a1a1a", // Example header background color
          color: "#ffce4a", // Header text color
        },
      },
    },
    // Override the DialogContent styles
    MuiDialogContent: {
      styleOverrides: {
        root: {
          padding: "16px",
          fontSize: "1rem",
          color: "#fff",
        },
      },
    },
    // Override the DialogActions styles
    MuiDialogActions: {
      styleOverrides: {
        root: {
          padding: "8px 16px",
          justifyContent: "flex-end",
        },
      },
    },
    MuiTable: {
      styleOverrides: {
        root: {
          // Custom table container styles
          borderCollapse: "collapse",
          borderSpacing: 0,
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: "#1a1a1a", // Header background color
          color: "#ffce4a", // Header text color
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          padding: "12px 16px",
          borderBottom: "4px solid #000",
          verticalAlign: 'top', // Aligns text to the top of the table cell
        },
        head: {
          fontWeight: "bold",
          color: "#ffce4a", // Header text color
          fontSize: "1rem",
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          "&:nth-of-type(even)": {
            backgroundColor: "#1a1a1a", // Zebra striping for even rows
          },
        },
      },
    },
  },
});

export default theme;
