webpackHotUpdate("static/development/pages/index.js",{

/***/ "./components/Tweets.js":
/*!******************************!*\
  !*** ./components/Tweets.js ***!
  \******************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _babel_runtime_corejs2_helpers_esm_classCallCheck__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @babel/runtime-corejs2/helpers/esm/classCallCheck */ "./node_modules/@babel/runtime-corejs2/helpers/esm/classCallCheck.js");
/* harmony import */ var _babel_runtime_corejs2_helpers_esm_createClass__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @babel/runtime-corejs2/helpers/esm/createClass */ "./node_modules/@babel/runtime-corejs2/helpers/esm/createClass.js");
/* harmony import */ var _babel_runtime_corejs2_helpers_esm_possibleConstructorReturn__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @babel/runtime-corejs2/helpers/esm/possibleConstructorReturn */ "./node_modules/@babel/runtime-corejs2/helpers/esm/possibleConstructorReturn.js");
/* harmony import */ var _babel_runtime_corejs2_helpers_esm_getPrototypeOf__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @babel/runtime-corejs2/helpers/esm/getPrototypeOf */ "./node_modules/@babel/runtime-corejs2/helpers/esm/getPrototypeOf.js");
/* harmony import */ var _babel_runtime_corejs2_helpers_esm_assertThisInitialized__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @babel/runtime-corejs2/helpers/esm/assertThisInitialized */ "./node_modules/@babel/runtime-corejs2/helpers/esm/assertThisInitialized.js");
/* harmony import */ var _babel_runtime_corejs2_helpers_esm_inherits__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @babel/runtime-corejs2/helpers/esm/inherits */ "./node_modules/@babel/runtime-corejs2/helpers/esm/inherits.js");
/* harmony import */ var _babel_runtime_corejs2_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @babel/runtime-corejs2/helpers/esm/defineProperty */ "./node_modules/@babel/runtime-corejs2/helpers/esm/defineProperty.js");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! react */ "./node_modules/react/index.js");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_7___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_7__);
/* harmony import */ var vertx3_eventbus_client__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! vertx3-eventbus-client */ "./node_modules/vertx3-eventbus-client/vertx-eventbus.js");
/* harmony import */ var vertx3_eventbus_client__WEBPACK_IMPORTED_MODULE_8___default = /*#__PURE__*/__webpack_require__.n(vertx3_eventbus_client__WEBPACK_IMPORTED_MODULE_8__);







var _jsxFileName = "/Users/rudra/Projects/tweets-aggregator/front/components/Tweets.js";



var Tweets =
/*#__PURE__*/
function (_React$Component) {
  Object(_babel_runtime_corejs2_helpers_esm_inherits__WEBPACK_IMPORTED_MODULE_5__["default"])(Tweets, _React$Component);

  function Tweets() {
    var _getPrototypeOf2;

    var _this;

    Object(_babel_runtime_corejs2_helpers_esm_classCallCheck__WEBPACK_IMPORTED_MODULE_0__["default"])(this, Tweets);

    for (var _len = arguments.length, args = new Array(_len), _key = 0; _key < _len; _key++) {
      args[_key] = arguments[_key];
    }

    _this = Object(_babel_runtime_corejs2_helpers_esm_possibleConstructorReturn__WEBPACK_IMPORTED_MODULE_2__["default"])(this, (_getPrototypeOf2 = Object(_babel_runtime_corejs2_helpers_esm_getPrototypeOf__WEBPACK_IMPORTED_MODULE_3__["default"])(Tweets)).call.apply(_getPrototypeOf2, [this].concat(args)));

    Object(_babel_runtime_corejs2_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_6__["default"])(Object(_babel_runtime_corejs2_helpers_esm_assertThisInitialized__WEBPACK_IMPORTED_MODULE_4__["default"])(_this), "state", {
      data: {
        tweets: []
      }
    });

    Object(_babel_runtime_corejs2_helpers_esm_defineProperty__WEBPACK_IMPORTED_MODULE_6__["default"])(Object(_babel_runtime_corejs2_helpers_esm_assertThisInitialized__WEBPACK_IMPORTED_MODULE_4__["default"])(_this), "eventBus", null);

    return _this;
  }

  Object(_babel_runtime_corejs2_helpers_esm_createClass__WEBPACK_IMPORTED_MODULE_1__["default"])(Tweets, [{
    key: "componentDidMount",
    // Lifecycle
    value: function componentDidMount() {
      var data = this.state.data;
      this.eventBus = new vertx3_eventbus_client__WEBPACK_IMPORTED_MODULE_8___default.a('http://localhost:8080/eventbus');
      var eb = this.eventBus;
      var self = this;

      eb.onopen = function () {
        eb.registerHandler('chat.message', function (err, message) {
          if (!err) {
            data = self.state.data;
            var messages = self.state.data.messages;

            if (data.author !== message.body.author) {
              messages.push(message.body);
              self.setState({
                data: {
                  author: data.author,
                  messages: messages
                }
              });
            }
          }
        });
      };
    }
  }, {
    key: "componentDidUpdate",
    value: function componentDidUpdate() {
      if (this.refs.message) this.refs.message.refs.input.focus();
      if (this.refs.messages_scroll_area) this.refs.messages_scroll_area.scrollTop = this.refs.messages_scroll_area.scrollHeight;
    }
  }, {
    key: "setAuthor",
    value: function setAuthor() {
      var author = this.refs.author.refs.input.value.trim();
      if (!author) return;
      this.refs.author.refs.input.value = '';
      var messages = this.state.data.messages;
      this.setState({
        data: {
          author: author,
          messages: messages
        }
      });
    }
  }, {
    key: "createMessage",
    value: function createMessage() {
      var data = this.state.data;
      var messages = data.messages;
      var eb = this.eventBus;
      var message_text = this.refs.message.refs.input.value.trim();
      if (!message_text) return;
      var message = {
        message: message_text,
        author: data.author
      }; // Send message out

      eb.publish('chat.message', message); // Render to browser

      messages.push(message);
      this.setState({
        data: {
          author: data.author,
          messages: messages
        }
      });
      this.refs.message.refs.input.value = '';
    }
  }, {
    key: "handleSubmit",
    value: function handleSubmit(e) {
      e.preventDefault();
      var data = this.state.data;
      if (data.author) this.createMessage();else this.setAuthor();
    }
  }, {
    key: "render",
    value: function render() {
      var data = this.state.data;
      return react__WEBPACK_IMPORTED_MODULE_7___default.a.createElement("div", {
        __source: {
          fileName: _jsxFileName,
          lineNumber: 99
        },
        __self: this
      }, "Hello");
    }
  }]);

  return Tweets;
}(react__WEBPACK_IMPORTED_MODULE_7___default.a.Component);

/* harmony default export */ __webpack_exports__["default"] = (Tweets);

/***/ })

})
//# sourceMappingURL=index.js.f7d5bcd360b82a9956fc.hot-update.js.map