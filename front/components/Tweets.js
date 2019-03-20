import EventBus from 'vertx3-eventbus-client'

class Tweets extends React.Component {

  // Variables

  state = {
    data: {
      tweets: []
    }
  }

  eventBus = null
  isMounted = false;

  // Lifecycle

  componentDidMount() {
    this.isMounted = true

    const eb = new EventBus('http://localhost:8080/eventbus')

    eb.onopen = () => {
      eb.registerHandler('api.tweets', this.tweetsHandler)
    }
  }

  componentWillUnmount() {
    this.isMounted = false;
  }

  // Private

  tweetsHandler = (err, msg) => {
    if (!this.isMounted) return
    if (err) return

    const tweets = [msg.body, ...this.state.data.tweets].slice(0, 10)
    this.setState({
      data: { tweets }
    })
  }

  // Render

  render() {
    console.log(this.state.data.tweets);
    
    return (
      <ul>
        {this.state.data.tweets.map(tweet => {
          return (
            <li key={ tweet.id }>
              <p>{ tweet.text }</p>
              <hr/>
            </li>
          )
        })}
      </ul>
    )
  }
}

export default Tweets