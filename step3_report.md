## Which API was chosen and why

Weather API, chosen because its simplicity and non-intrusiveness to integrate. Our alarm clock app doesn't really rely on the internet for anything because everything can be stored locally.

The API is provided by [Keskkonnaagentuur](www.ilmateenistus.ee/ilma_andmed/xml/).

## Example API endpoint used

The weather station provides forecasts and observations, we use observations for the current weather at the time of the alarm screen opening.

## Error handling strategy

We don't display weather if there was an error with getting the weather from the API.