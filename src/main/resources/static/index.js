; (function () {

    const socketUrl = `ws://${window.location.host}/messages`;

    let ws;

    function byId(id) {
        return document.getElementById(id);
    }

    function firstChildElement(node) {
        let n = node.firstChild;
        while (n.nodeType !== Node.ELEMENT_NODE) {
            n = n.nextSibling;
        }
        return n;
    }

    let divHome = byId("divHome");
    let divCreate = byId("divCreate");
    let divJoin = byId("divJoin");
    let divPlay = byId("divPlay");
    let divRole = byId("divRole");
    let divRoster = byId("divRoster");
    let divStartSetup = byId("divStartSetup");
    let divStart = byId("divStart");
    let divErr = byId("divErr");
    let divNominate = byId("divNominate");
    let divVote = byId("divVote");
    let divMessages = byId("divMessages");

    let ulErrors = byId("ulErrors");

    let txtModeratorName = byId("txtModeratorName");
    let txtPlayerName = byId("txtPlayerName");
    let txtGameCode = byId("txtGameCode");

    let spnGameCode = byId("spnGameCode");
    let spnPlayerName = byId("spnPlayerName");

    let gameHeader = byId("gameHeader");

    // session management
    let session = (function () {
        var storage = window.sessionStorage;
        return {
            playerName: function (name) {
                if (arguments.length === 0) {
                    return storage.getItem("playerName");
                }
                storage.setItem("playerName", name);
            },
            gameCode: function (code) {
                if (arguments.length === 0) {
                    return storage.getItem("gameCode");
                }
                storage.setItem("gameCode", code);
            },
            moderator: function (mod) {
                if (arguments.length === 0) {
                    return storage.getItem("moderator") === "true";
                }
                storage.setItem("moderator", mod);
            },
            selectedPlayers: function (players) {
                if (arguments.length === 0) {
                    let json = storage.getItem("selectedPlayers");
                    if (json && json.length > 0) {
                        return JSON.parse(json);
                    }
                    return [];
                }
                storage.setItem("selectedPlayers", JSON.stringify(players));
            }
        };
    })();

    // action panel
    let actionPanel = {
        prompt: null,
        panel: byId("divAction"),
        promptHeader: byId("promptHeader"),
        divActionPlayers: byId("divActionPlayers"),
        init: function (gameState) {

            let prompt = gameState.prompt;

            if (gameState.gameStatus === "NIGHT" && prompt) {

                if (!this.areEqual(prompt)) {

                    this.prompt = prompt;

                    this.promptHeader.textContent = prompt.prompt;
                    let selectedPlayers = session.selectedPlayers();

                    let html = "";
                    gameState.players.forEach(function (p) {

                        if (!prompt.canSelectSelf && p.name === session.playerName()) {
                            return;
                        }

                        let selected = selectedPlayers.indexOf(p.name) >= 0 ? 'class="selected"' : '';
                        html += `<div class="action-row">
                        <button onclick="routeClick(this, 'select');"${selected}>${p.name}</button>
                        </div>`;
                    });
                    this.divActionPlayers.innerHTML = html;
                }

                this.panel.classList.remove("hidden");

            } else {
                this.panel.classList.add("hidden");
            }
        },
        areEqual: function (prompt) {
            return (prompt === null && this.prompt === null)
                || (prompt !== null && this.prompt !== null
                    && prompt.version === this.prompt.version);
        },
        select: function (btn) {

            let selectedPlayers = session.selectedPlayers();
            let playerName = btn.innerText.trim();

            if (btn.classList.contains("selected")) {
                let index = selectedPlayers.indexOf(playerName);
                selectedPlayers.splice(index, 1);
                btn.classList.remove("selected");
            } else {
                btn.classList.add("selected");
                selectedPlayers.push(playerName);
            }

            if (selectedPlayers.length === this.prompt.count) {

                move({
                    type: "USE_ABILITY",
                    ability: this.prompt.ability,
                    names: selectedPlayers
                });

                // clear
                selectedPlayers = [];
                this.prompt = null;
            }

            session.selectedPlayers(selectedPlayers);
        }
    };

    function hideAll() {
        divHome.classList.add("hidden");
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

    function renderSetup(gameState) {
        if (gameState.gameStatus === "JOINABLE" && session.moderator()) {
            divStartSetup.classList.remove("hidden");
            if (gameState.players.length >= 4) {
                var node = firstChildElement(divStartSetup);
                node.removeAttribute("disabled");
                node.setAttribute("title", "Ready to set up!");
            }
        } else {
            divStartSetup.classList.add("hidden");
        }
    }

    function renderStart(gameState) {
        if (gameState.gameStatus === "SETUP" && session.moderator()) {
            divStart.classList.remove("hidden");
        } else {
            divStart.classList.add("hidden");
        }
    }

    function renderRole(role) {
        if (role) {
            divRole.innerHTML = `<div><label>Role:</label>${role.name}</div>
                <p>${role.description}</p>`;
            divRole.classList.remove("hidden");
        } else {
            divRoster.classList.add("hidden");
        }
    }

    function renderVote(gameState) {
        if (gameState.canVote) {
            byId("divVoteMessage").innerHTML =
                `<div>${gameState.nominator} nominated ${gameState.nominated}.</div>
            <div>Execute ${gameState.nominated}?</div>`;
            divVote.classList.remove("hidden");
        } else {
            divVote.classList.add("hidden");
        }
    }

    function renderMessages(playerMessages) {
        if (playerMessages.length > 0) {
            let html = '<h3>Private Messages</h3>';
            playerMessages.forEach(function (msg) {
                html += `<div>${msg}</div>`
            });
            divMessages.innerHTML = html;
            divMessages.classList.remove("hidden");
        } else {
            divMessages.classList.add("hidden");
        }
    }

    function getAdminControls(player, arrange, kill) {
        if (arrange) {
            return `<div>
            <a href="#nowhere" class="href-btn" onclick="return routeClick(this, 'movePlayerUp');">
                <img src="/images/up.svg" class="connection-status" alt="move player up" title="move player up">
            </a>
            </div>`;
        } else if (kill && player.status === "ALIVE") {
            return `<div>
            <button onclick="return routeClick(this, 'kill');">kill</button>
            </div>`;
        }
        return "";
    }

    function renderRoster(gameState, kill) {

        let arrange = session.moderator() && gameState.gameStatus === "SETUP";

        let html = "";

        gameState.players.forEach(function (player) {

            let controls = getAdminControls(player, arrange, kill);

            if (gameState.canNominate
                && session.playerName() !== player.name
                && gameState.possibleNominations.indexOf(player.name) >= 0) {
                controls += `<div>
                <button onclick="routeClick(this, 'nominate');">nominate</button>
                </div>`;
            }

            html += `<div class="player">
                <div>${player.name}</div>
                <div>
                    <img src="/images/${player.connected ? "connected.svg" : "disconnected.svg"}" class="connection-status">
                </div>
                <div>${player.status}</div>
                ${controls}
            </div>`;
        });

        divRoster.innerHTML = html;
        divRoster.classList.remove("hidden");
    }

    let gameStatusUI = {
        "JOINABLE": { header: "Waiting for others to join...", bodyClass: "", modCanKill: false },
        "SETUP": { header: "Moderator is setting up.", bodyClass: "", modCanKill: false },
        "DAY_NOMINATE": { header: "Day: Accepting Nominations", bodyClass: "day", modCanKill: true },
        "DAY_VOTE": { header: "Day: Vote", bodyClass: "day", modCanKill: true },
        "NIGHT": { header: "Night", bodyClass: "night", modCanKill: true },
        "EVIL_WINS": { header: "Evil Wins", bodyClass: "evil-wins", modCanKill: false },
        "GOOD_WINS": { header: "Good Wins", bodyClass: "good-wins", modCanKill: false }
    };

    function render(gameState) {

        let settings = gameStatusUI[gameState.gameStatus];
        gameHeader.textContent = settings.header;
        document.body.className = settings.bodyClass;

        renderSetup(gameState);
        renderStart(gameState);
        renderRole(gameState.role);
        renderRoster(gameState, settings.modCanKill && session.moderator());
        if (gameState.canNominate) {
            divNominate.classList.remove("hidden");
        } else {
            divNominate.classList.add("hidden");
        }
        renderVote(gameState);
        actionPanel.init(gameState);
        renderMessages(gameState.playerMessages);
    }

    function ack(msg) {

        session.playerName(msg.playerName);
        session.gameCode(msg.gameCode);
        session.moderator(msg.moderator);

        spnGameCode.textContent = msg.gameCode;
        spnPlayerName.textContent = msg.playerName;

        hideAll();
        divPlay.classList.remove("hidden");
    }

    function connect(playerName, gameCode) {

        ws = new WebSocket(socketUrl);

        ws.onopen = function () {
            let msg = {
                playerName: playerName,
                gameCode: gameCode
            }
            ws.send(JSON.stringify(msg));
        };

        ws.onmessage = function (message) {
            let msg = JSON.parse(message.data);
            console.log(msg);
            if (msg.type === "ACK") {
                ack(msg);
            } else {
                render(msg);
            }
        };

        ws.onclose = function () {
            console.log("ws closed.");
        };

        ws.onerror = function (err) {
            console.log("ws err:", err);
        };
    }

    function move(m) {
        let msg = {
            playerName: session.playerName(),
            gameCode: session.gameCode(),
            move: m
        }
        ws.send(JSON.stringify(msg));
    }

    function checkSession() {
        if (session.gameCode()) {
            connect(session.playerName(), session.gameCode());
        }
    }

    var actions = {
        showCreate: function () {
            divHome.classList.add("hidden");
            divCreate.classList.remove("hidden");
        },
        showJoin: function () {
            divHome.classList.add("hidden");
            divJoin.classList.remove("hidden");
        },
        cancel: function () {
            hideAll();
            divHome.classList.remove("hidden");
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
                .catch(showErr)
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
            if (gameCode.length !== 4) {
                errors.push("Game code must be four characters.")
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
                .catch(showErr)
                .finally(function () {
                    evt.target.removeAttribute("disabled");
                });
        },
        startSetup: function () {
            move({ type: "SETUP" });
        },
        start: function () {

            let orderedPlayers = [];

            for (let i = 0; i < divRoster.childNodes.length; i++) {
                let node = firstChildElement(divRoster.childNodes[i]);
                orderedPlayers.push(node.textContent.trim());
            }
            move({ type: "START", names: orderedPlayers });
        }
    };

    let clickHandlers = {
        movePlayerUp: function (href) {
            let div = href.parentElement.parentElement;
            div.parentElement.insertBefore(div, div.previousSibling);
        },
        kill: function (href) {

            if (!confirm("You can't un-kill someone. Are you sure?")) {
                return;
            }

            let div = href.parentElement.parentElement;
            let node = firstChildElement(div);

            console.log(node.textContent.trim());

            move({ type: "KILL", names: [node.textContent.trim()] });
        },
        nominate: function (btn) {

            let div = btn.parentElement.parentElement;
            let node = firstChildElement(div);

            move({ type: "NOMINATE", names: [node.textContent.trim()] })
        },
        declineNomination: function (_btn) {
            move({ type: "NOMINATE", names: [] })
        },
        select: function (btn) {
            actionPanel.select(btn);
        },
        vote: function (_btn, confirmed) {
            move({ type: "VOTE", confirmed: confirmed });
        }
    };

    window.routeClick = function (elem, name, arg) {
        clickHandlers[name](elem, arg);
        return false;
    };

    // process DOM and check for state
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