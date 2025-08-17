module.exports = {
  networks: {
    development: {
      host: "127.0.0.1",
      port: 7545,
      network_id: "*",
      chainId: 1337,
      // remove explicit gas; let Truffle estimate
      // gasPrice: 20000000000, // keep if you want, but not required locally
    },
  },

  compilers: {
    solc: {
      version: "0.8.21",
      settings: {
        optimizer: {
          enabled: true,
          runs: 500   // try 200 first; if still failing, bump to 500 or 1000
        },
        evmVersion: "london" // good default for Ganache
      }
    }
  }
};
