; (function () {

    const socketUrl = "ws://localhost:8080/messages"

    let ws;
    let storage = window.sessionStorage;

    function byId(id) {
        return document.getElementById(id);
    }

    let divStart = byId("divStart");
    let divCreate = byId("divCreate");
    let divJoin = byId("divJoin");
    let divPlay = byId("divPlay");
    let divRoster = byId("divRoster");
    let divStartSetup = byId("divStartSetup");
    let divErr = byId("divErr");
    let ulErrors = byId("ulErrors");

    let txtModeratorName = byId("txtModeratorName");
    let txtPlayerName = byId("txtPlayerName");
    let txtGameCode = byId("txtGameCode");

    let spnGameCode = byId("spnGameCode");
    let spnPlayerName = byId("spnPlayerName");
    let spnGameStatus = byId("spnGameStatus");

    let gameHeader = byId("gameHeader");

    function hideAll() {
        divStart.classList.add("hidden");
        divCreate.classList.add("hidden");
        divJoin.classList.add("hidden");
        divPlay.classList.add("hidden");
        divErr.classList.add("hidden");
    }

    function showErr(errors) {
        let html = "";
        if (typeof errors === "string") {
            html = `<li>${errors}</li>`;
        } else if (errors.constructor === Array) {
            errors.forEach(function (err) {
                html += `<li>${err}</li>`;
            });
        } else {
            console.log(errors);
            html = "<li>Unknown Error.</li>";
        }

        ulErrors.innerHTML = html;

        divErr.classList.remove("hidden");
    }

    function handleNetworkErr(err) {
        console.log(err);
    }

    function render(msg) {

        spnGameStatus.textContent = msg.gameStatus;
        if (msg.gameStatus === "JOINABLE") {
            gameHeader.textContent = "Waiting for others to join...";
        } else if (msg.gameStatus === "SETUP") {
            gameHeader.textContent = "Moderator is setting up.";
        } else {
            gameHeader.textContent = msg.gameStatus;
        }

        let html = "";
        msg.players.forEach(function (player) {
            html += `<div>${player.name} ${player.connected}</div>`;
        });
        divRoster.innerHTML = html;
    }

    function ack(msg) {

        storage.setItem("playerName", msg.playerName);
        storage.setItem("gameCode", msg.gameCode);
        storage.setItem("moderator", msg.moderator);

        spnGameCode.textContent = msg.gameCode;
        spnPlayerName.textContent = msg.playerName;

        hideAll();

        divPlay.classList.remove("hidden");

        if (msg.moderator) {
            divStartSetup.classList.remove("hidden");
        }
    }

    function connect(playerName, gameCode) {

        ws = new WebSocket(socketUrl);

        ws.onopen = function () {
            let msg = {
                playerName: playerName,
                gameCode: gameCode,
                type: "CONNECT"
            }
            ws.send(JSON.stringify(msg));
        };

        ws.onmessage = function (message) {
            let msg = JSON.parse(message.data);
            console.log(msg);
            switch (msg.type) {
                case "ACK":
                    ack(msg);
                    break;
                case "GAME_STATE":
                    render(msg);
                    break;
            }
        };

        ws.onclose = function () {
            console.log("ws closed.");
        };

        ws.onerror = function (err) {
            console.log("ws err:", err);
        };
    }

    function checkSession() {
        if (storage.getItem("gameCode")) {
            connect(storage.getItem("playerName"), storage.getItem("gameCode"));
        }
    }

    var actions = {
        showCreate: function () {
            divStart.classList.add("hidden");
            divCreate.classList.remove("hidden");
        },
        showJoin: function () {
            divStart.classList.add("hidden");
            divJoin.classList.remove("hidden");
        },
        cancel: function () {
            hideAll();
            divStart.classList.remove("hidden");
        },
        create: function (evt) {

            evt.target.setAttribute("disabled", true);

            let moderatorName = txtModeratorName.value.trim();
            if (moderatorName.length === 0) {
                evt.target.removeAttribute("disabled");
                showErr("Moderator name is required.");
                return;
            }

            let config = {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    playerName: moderatorName
                })
            };

            fetch("/create", config)
                .then(function (resp) {
                    resp.json().then(function (msg) {
                        if (msg.status === "SUCCESS") {
                            connect(moderatorName, msg.value.code);
                        } else if (msg.status === "INVALID") {
                            showErr(msg.errors);
                        } else {
                            showErr(msg);
                        }
                    });
                })
                .catch(handleNetworkErr)
                .finally(function () {
                    evt.target.removeAttribute("disabled");
                });
        },
        join: function (evt) {

            evt.target.setAttribute("disabled", true);

            let errors = [];

            let playerName = txtPlayerName.value.trim();
            if (playerName.length === 0) {
                errors.push("Player name is required.")
            }

            let gameCode = txtGameCode.value.trim();
            if (gameCode.length === 0) {
                errors.push("Game code is required.")
            }

            if (errors.length > 0) {
                evt.target.removeAttribute("disabled");
                showErr(errors);
                return;
            }

            let config = {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    playerName: playerName,
                    gameCode: gameCode
                })
            };

            fetch("/join", config)
                .then(function (resp) {
                    resp.json().then(function (msg) {
                        if (msg.status === "SUCCESS") {
                            connect(playerName, gameCode);
                        } else if (msg.status === "INVALID") {
                            // we show the error, but then try to reconnect
                            showErr(msg.errors);
                            connect(playerName, gameCode);
                        } else if (msg.status === "NOT_FOUND") {
                            showErr(msg.errors);
                        } else {
                            showErr(msg);
                        }
                    });
                })
                .catch(handleNetworkErr)
                .finally(function () {
                    evt.target.removeAttribute("disabled");
                });
        },
        startSetup: function () {
            let msg = {
                playerName: storage.getItem("playerName"),
                gameCode: storage.getItem("gameCode"),
                type: "START_SETUP"
            }
            ws.send(JSON.stringify(msg));
        }
    };

    function handleAction(evt) {
        let action = this.getAttribute("data-action");
        actions[action].call(this, evt);
    }

    let clickables = document.querySelectorAll("*[data-action]");
    for (let i = 0; i < clickables.length; i++) {
        clickables[i].addEventListener("click", handleAction);
    }

    checkSession();

})();