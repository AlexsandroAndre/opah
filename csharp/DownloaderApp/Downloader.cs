// Downloader.cs

using System.Collections.Concurrent;

class Downloader
{
    private static readonly ConcurrentBag<string> cache = new ConcurrentBag<string>();
    private static readonly HttpClient client = new HttpClient
    {
        Timeout = TimeSpan.FromSeconds(30)
    };

    public static async Task Main(string[] args)
    {
        var tasks = new List<Task>();

        for (int i = 0; i < 10; i++)
        {
            string url = $"https://example.com/data/{i}";
            tasks.Add(DownloadAsync(url));
        }

        Console.WriteLine("Downloads started");

        await Task.WhenAll(tasks);

        Console.WriteLine("All downloads finished");
        Console.WriteLine("Cache size: " + cache.Count);
    }

    private static async Task DownloadAsync(string url)
    {
        try
        {
            var data = await client.GetStringAsync(url);
            cache.Add(data);
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine($"Error downloading {url}: {ex.Message}");
        }
    }
}